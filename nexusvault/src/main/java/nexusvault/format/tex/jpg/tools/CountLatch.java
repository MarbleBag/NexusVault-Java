package nexusvault.format.tex.jpg.tools;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public final class CountLatch {

	private static final class Sync extends AbstractQueuedSynchronizer {

		private static final long serialVersionUID = 1L;

		Sync(int count) {
			setState(count);
		}

		int getCount() {
			return getState();
		}

		@Override
		protected int tryAcquireShared(int acquires) {
			return getState() == 0 ? 1 : -1;
		}

		protected int acquireNonBlocking(int acquires) {
			// increment count
			for (;;) {
				final int c = getState();
				final int nextc = c + 1;
				if (compareAndSetState(c, nextc)) {
					return 1;
				}
			}
		}

		@Override
		protected boolean tryReleaseShared(int releases) {
			// Decrement count; signal when transition to zero
			for (;;) {
				final int c = getState();
				if (c == 0) {
					return false;
				}
				final int nextc = c - 1;
				if (compareAndSetState(c, nextc)) {
					return nextc == 0;
				}
			}
		}
	}

	private final Sync sync;

	public CountLatch(int count) {
		this.sync = new Sync(count);
	}

	public void awaitZero() throws InterruptedException {
		this.sync.acquireSharedInterruptibly(1);
	}

	public boolean awaitZero(long timeout, TimeUnit unit) throws InterruptedException {
		return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
	}

	public void increment() {
		this.sync.acquireNonBlocking(1);
	}

	public void decrement() {
		this.sync.releaseShared(1);
	}

	@Override
	public String toString() {
		return super.toString() + "[Count = " + this.sync.getCount() + "]";
	}

}