package xyz.peppie.versatilelogger.io.local;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import xyz.peppie.versatilelogger.chat.ChatCategory;

@Slf4j
@Singleton
public class LocalLogWriter
{
	public static final Path PLUGIN_ROOT = RuneLite.RUNELITE_DIR.toPath().resolve("versatile-logger");

	private static final DateTimeFormatter SESSION_STAMP =
		DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

	private volatile ExecutorService executor;

	// Only ever accessed from tasks running on `executor`.
	private final Map<ChatCategory, BufferedWriter> perCategoryWriters = new HashMap<>();
	private BufferedWriter singleFileWriter;
	private Path accountDir;
	private boolean perCategoryFiles;
	private String sessionStamp;

	public void startUp(ExecutorService executor)
	{
		this.executor = executor;
	}

	public void beginSession(String accountName, boolean perCategoryFiles)
	{
		if (executor == null)
		{
			return;
		}
		String stamp = SESSION_STAMP.format(LocalDateTime.now());
		executor.execute(() ->
		{
			closeAll();
			this.accountDir = PLUGIN_ROOT.resolve(sanitizeFolderName(accountName));
			this.perCategoryFiles = perCategoryFiles;
			this.sessionStamp = stamp;
			log.debug("Local log session started for {}", accountDir);
		});
	}

	public void writeLine(ChatCategory category, String line)
	{
		if (executor == null)
		{
			return;
		}
		executor.execute(() -> doWrite(category, line));
	}

	public void endSession()
	{
		if (executor == null)
		{
			return;
		}
		executor.execute(this::closeAll);
	}

	private void doWrite(ChatCategory category, String line)
	{
		if (accountDir == null)
		{
			log.debug("Dropping local log line for {}, no session started yet", category);
			return;
		}
		try
		{
			BufferedWriter writer = perCategoryFiles ? perCategoryWriter(category) : singleFileWriter();
			writer.write(line);
			writer.newLine();
			writer.flush();
		}
		catch (IOException e)
		{
			log.debug("Failed to write local log line for category {}", category, e);
		}
	}

	private BufferedWriter perCategoryWriter(ChatCategory category) throws IOException
	{
		BufferedWriter writer = perCategoryWriters.get(category);
		if (writer != null)
		{
			return writer;
		}

		Path categoryDir = accountDir.resolve(category.getFolderSlug());
		Files.createDirectories(categoryDir);
		Path file = categoryDir.resolve(sessionStamp + "-" + category.getFolderSlug() + "_log.txt");
		writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		perCategoryWriters.put(category, writer);
		return writer;
	}

	private BufferedWriter singleFileWriter() throws IOException
	{
		if (singleFileWriter != null)
		{
			return singleFileWriter;
		}

		Files.createDirectories(accountDir);
		Path file = accountDir.resolve(sessionStamp + ".txt");
		singleFileWriter = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		return singleFileWriter;
	}

	private void closeAll()
	{
		perCategoryWriters.values().forEach(this::closeQuietly);
		perCategoryWriters.clear();
		closeQuietly(singleFileWriter);
		singleFileWriter = null;
		accountDir = null;
	}

	private void closeQuietly(BufferedWriter writer)
	{
		if (writer == null)
		{
			return;
		}
		try
		{
			writer.close();
		}
		catch (IOException e)
		{
			log.debug("Failed to close local log writer", e);
		}
	}

	private static String sanitizeFolderName(String name)
	{
		return name == null ? "unknown" : name.replaceAll("[\\\\/:*?\"<>|]", "_");
	}
}
