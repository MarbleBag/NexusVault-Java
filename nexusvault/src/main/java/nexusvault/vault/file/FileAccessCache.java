package nexusvault.vault.file;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class FileAccessCache {

	static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		DefaultThreadFactory() {
			final SecurityManager s = System.getSecurityManager();
			this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			final Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0);
			t.setDaemon(true);
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}

	private final long cacheTime;
	private final Path filePath;

	private final ExecutorService executor;

	private volatile long lastUsed;
	private volatile boolean expiring;
	private volatile boolean taskShutdown;

	private final Object lock = new Object();

	private SeekableByteChannel stream;
	private final EnumSet<StandardOpenOption> fileAccessOption;

	public FileAccessCache(long cacheTime, Path filePath, EnumSet<StandardOpenOption> fileAccessOption) {
		if (cacheTime <= 0) {
			throw new IllegalArgumentException();
		}
		if (filePath == null) {
			throw new IllegalArgumentException();
		}
		if (fileAccessOption == null) {
			throw new IllegalArgumentException();
		}

		this.cacheTime = cacheTime;
		this.filePath = filePath;
		this.fileAccessOption = fileAccessOption;
		this.executor = new ThreadPoolExecutor(0, 1, 15L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new DefaultThreadFactory());
		this.taskShutdown = true;
	}

	public void shutDown() {
		this.executor.shutdownNow();
	}

	public boolean isShutDown() {
		return this.executor.isShutdown();
	}

	public SeekableByteChannel getFileAccess() throws IOException {
		synchronized (this.lock) {
			this.lastUsed = System.currentTimeMillis();
			this.expiring = false;
			if (this.stream == null || !this.stream.isOpen()) {
				if (this.fileAccessOption.contains(StandardOpenOption.CREATE) || this.fileAccessOption.contains(StandardOpenOption.CREATE_NEW)) {
					final var parent = this.filePath.getParent();
					if (parent != null && !Files.exists(parent)) {
						Files.createDirectories(parent);
					}
				}
				this.stream = Files.newByteChannel(this.filePath, this.fileAccessOption);
			}
		}
		return this.stream;
	}

	public Path getSource() {
		return this.filePath;
	}

	private boolean tryToCloseChannel() {
		synchronized (this.lock) {
			if (!this.expiring) {
				return false;
			}
			if (System.currentTimeMillis() < this.cacheTime + this.lastUsed) {
				return false;
			}
			try {
				this.stream.close();
			} catch (final RuntimeException e) {
				e.printStackTrace();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	}

	private void checkInspector() {
		synchronized (this.lock) {
			if (this.taskShutdown) {
				final Runnable timer = () -> {
					while (true) {
						if (this.expiring) {
							final long time = System.currentTimeMillis();
							if (time > this.lastUsed + this.cacheTime) {
								synchronized (this.lock) {
									if (tryToCloseChannel()) {
										this.taskShutdown = true;
										break;
									}
								}
							}
						}

						try {
							if (this.executor.isShutdown()) {
								return;
							}
							Thread.sleep(1000);
						} catch (final InterruptedException e) {
							if (this.executor.isShutdown()) {
								return;
							}
						}
					}
				};

				this.taskShutdown = false;
				this.executor.execute(timer);
			}
		}
	}

	public void startExpiring() {
		if (this.expiring) {
			return;
		}

		this.lastUsed = System.currentTimeMillis();
		this.expiring = true;
		checkInspector();
	}

}