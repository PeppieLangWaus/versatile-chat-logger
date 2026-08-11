package xyz.peppie.versatilelogger.io.local;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Deletes local log files/folders older than the configured retention window. Every candidate
 * path is verified to live inside {@link LocalLogWriter#PLUGIN_ROOT} before deletion, so a
 * malformed or unexpected filename can never cause a delete outside this plugin's own directory.
 * Intended to be run off the client thread (the plugin dispatches it onto the same executor used
 * for local file writes).
 */
@Slf4j
@Singleton
public class LogRetentionManager
{
	private static final DateTimeFormatter SESSION_STAMP =
		DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	private static final int STAMP_LENGTH = "yyyy-MM-dd_HH-mm-ss".length();

	public void run(Path root, int retentionDays)
	{
		if (retentionDays <= 0 || !Files.isDirectory(root))
		{
			return;
		}

		LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

		try (DirectoryStream<Path> accounts = Files.newDirectoryStream(root))
		{
			for (Path accountDir : accounts)
			{
				if (Files.isDirectory(accountDir))
				{
					cleanAccountDir(accountDir, cutoff);
				}
			}
		}
		catch (IOException e)
		{
			log.debug("Failed to walk log root for retention cleanup", e);
		}
	}

	private void cleanAccountDir(Path accountDir, LocalDateTime cutoff)
	{
		try (DirectoryStream<Path> entries = Files.newDirectoryStream(accountDir))
		{
			for (Path entry : entries)
			{
				if (Files.isDirectory(entry))
				{
					cleanCategoryDir(entry, cutoff);
				}
				else
				{
					deleteIfStale(entry, cutoff);
				}
			}
		}
		catch (IOException e)
		{
			log.debug("Failed to walk account dir for retention cleanup: {}", accountDir, e);
		}
	}

	private void cleanCategoryDir(Path categoryDir, LocalDateTime cutoff)
	{
		boolean anyLeft = false;
		try (DirectoryStream<Path> entries = Files.newDirectoryStream(categoryDir))
		{
			for (Path entry : entries)
			{
				if (!deleteIfStale(entry, cutoff))
				{
					anyLeft = true;
				}
			}
		}
		catch (IOException e)
		{
			log.debug("Failed to walk category dir for retention cleanup: {}", categoryDir, e);
			return;
		}

		if (!anyLeft)
		{
			try
			{
				Files.deleteIfExists(categoryDir);
			}
			catch (IOException e)
			{
				log.debug("Failed to prune empty category dir: {}", categoryDir, e);
			}
		}
	}

	private boolean deleteIfStale(Path file, LocalDateTime cutoff)
	{
		LocalDateTime stamp = parseStamp(file.getFileName().toString());
		if (stamp == null || !stamp.isBefore(cutoff))
		{
			return false;
		}

		if (!isSafeToDelete(file))
		{
			log.debug("Refusing to delete path outside plugin root: {}", file);
			return false;
		}

		try
		{
			Files.deleteIfExists(file);
			log.debug("Deleted stale log file: {}", file);
			return true;
		}
		catch (IOException e)
		{
			log.debug("Failed to delete stale log file: {}", file, e);
			return false;
		}
	}

	private static boolean isSafeToDelete(Path file)
	{
		Path normalized = file.toAbsolutePath().normalize();
		Path root = LocalLogWriter.PLUGIN_ROOT.toAbsolutePath().normalize();
		return normalized.startsWith(root);
	}

	private static LocalDateTime parseStamp(String filename)
	{
		if (filename.length() < STAMP_LENGTH)
		{
			return null;
		}
		try
		{
			return LocalDateTime.parse(filename.substring(0, STAMP_LENGTH), SESSION_STAMP);
		}
		catch (DateTimeParseException e)
		{
			return null;
		}
	}
}
