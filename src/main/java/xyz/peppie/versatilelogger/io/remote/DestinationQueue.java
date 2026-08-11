package xyz.peppie.versatilelogger.io.remote;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

/**
 * Bounded, drop-oldest-on-overflow outbound queue for one remote destination URL. Draining
 * happens on a shared {@link java.util.concurrent.ScheduledExecutorService} owned by
 * {@link RemoteLogSender}, at a fixed interval, so a single busy destination can't be hammered
 * faster than that interval regardless of how quickly messages are enqueued.
 */
@Slf4j
class DestinationQueue
{
	private final Deque<Request> queue = new ArrayDeque<>();
	private final int maxSize;
	private ScheduledFuture<?> drainTask;

	DestinationQueue(int maxSize)
	{
		this.maxSize = maxSize;
	}

	synchronized void offer(Request request)
	{
		if (queue.size() >= maxSize)
		{
			queue.pollFirst();
			log.debug("Remote log destination queue full, dropping oldest queued request");
		}
		queue.addLast(request);
	}

	synchronized Request poll()
	{
		return queue.pollFirst();
	}

	void setDrainTask(ScheduledFuture<?> drainTask)
	{
		this.drainTask = drainTask;
	}

	void cancel()
	{
		if (drainTask != null)
		{
			drainTask.cancel(false);
		}
	}
}
