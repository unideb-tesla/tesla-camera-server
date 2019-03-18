public class Chain {

	private String id;
	private String name;
	private int length;
	private int intervalDuration;
	private int delay;

	public Chain(String id, String name, int length, int intervalDuration, int delay) {
		this.id = id;
		this.name = name;
		this.length = length;
		this.intervalDuration = intervalDuration;
		this.delay = delay;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getIntervalDuration() {
		return intervalDuration;
	}

	public void setIntervalDuration(int intervalDuration) {
		this.intervalDuration = intervalDuration;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	@Override
	public String toString() {
		return "Chain{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", length=" + length +
				", intervalDuration=" + intervalDuration +
				", delay=" + delay +
				'}';
	}

}
