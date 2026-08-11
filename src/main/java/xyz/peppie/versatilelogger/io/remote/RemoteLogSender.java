package xyz.peppie.versatilelogger.io.remote;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xyz.peppie.versatilelogger.dto.DiscordContentPayload;
import xyz.peppie.versatilelogger.dto.FullMessagePayload;

/**
 * Sends formatted messages to a user-configured remote HTTP endpoint. Every send goes through a
 * per-destination-URL {@link DestinationQueue}, drained at a fixed interval on a single
 * background thread, so this plugin never hammers a destination (e.g. a Discord webhook, subject
 * to its own rate limits) regardless of local chat volume. All network I/O happens via
 * {@link OkHttpClient#newCall(Request)}'s async {@code enqueue}, never on the client thread.
 */
@Slf4j
@Singleton
public class RemoteLogSender
{
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final long DRAIN_INTERVAL_MS = 500;
	private static final int MAX_QUEUE_SIZE = 50;

	private final OkHttpClient okHttpClient;
	private final Gson gson;
	private final Map<String, DestinationQueue> destinations = new ConcurrentHashMap<>();
	private volatile ScheduledExecutorService scheduler;

	@Inject
	RemoteLogSender(OkHttpClient okHttpClient, Gson gson)
	{
		this.okHttpClient = okHttpClient;
		this.gson = gson;
	}

	public void startUp()
	{
		scheduler = Executors.newSingleThreadScheduledExecutor();
	}

	public void shutDown()
	{
		destinations.values().forEach(DestinationQueue::cancel);
		destinations.clear();
		if (scheduler != null)
		{
			scheduler.shutdownNow();
			scheduler = null;
		}
	}

	public void sendInGameMessage(String url, String authTokenOrNull, String line)
	{
		enqueue(url, authTokenOrNull, new DiscordContentPayload(line));
	}

	public void sendFull(String url, String authTokenOrNull, FullMessagePayload payload)
	{
		enqueue(url, authTokenOrNull, payload);
	}

	private void enqueue(String url, String authTokenOrNull, Object body)
	{
		if (scheduler == null || url == null || url.isBlank())
		{
			return;
		}

		RequestBody requestBody = RequestBody.create(JSON, gson.toJson(body));
		Request.Builder builder = new Request.Builder()
			.url(url)
			.post(requestBody);
		if (authTokenOrNull != null && !authTokenOrNull.isBlank())
		{
			builder.header("Authorization", "Bearer " + authTokenOrNull);
		}

		DestinationQueue queue = destinations.computeIfAbsent(url, this::createDestination);
		queue.offer(builder.build());
	}

	private DestinationQueue createDestination(String url)
	{
		DestinationQueue queue = new DestinationQueue(MAX_QUEUE_SIZE);
		ScheduledFuture<?> drainTask = scheduler.scheduleAtFixedRate(
			() -> drain(queue), DRAIN_INTERVAL_MS, DRAIN_INTERVAL_MS, TimeUnit.MILLISECONDS);
		queue.setDrainTask(drainTask);
		return queue;
	}

	private void drain(DestinationQueue queue)
	{
		Request request = queue.poll();
		if (request == null)
		{
			return;
		}

		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.debug("Remote log request failed", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				if (!response.isSuccessful())
				{
					log.debug("Remote log request returned HTTP {}", response.code());
				}
				response.body().close();
			}
		});
	}
}
