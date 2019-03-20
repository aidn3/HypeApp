package net.hypixel.api.reply;

public abstract class AbstractReply {

	public long timeCache = -1;

	protected boolean throttle;
	protected boolean success;
	protected String cause;

	public boolean isThrottle() {
		return throttle;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getCause() {
		return cause;
	}

	public boolean isUpToDate() {
		// from cache, but it's been only 15 seconds
		return timeCache < 15000;
	}

	@Override
	public String toString() {
		return "AbstractReply{" +
				"throttle=" + throttle +
				", success=" + success +
				", cause='" + cause + '\'' +
				'}';
	}
}
