package app.velor.storage.vo;

public class RouteConnection {
	public RouteConnectionPoint src;
	public RouteConnectionPoint dst;
	public double distance = -1;

	public RouteConnection(RouteConnectionPoint src, RouteConnectionPoint dst) {
		super();
		this.src = src;
		this.dst = dst;
	}

	public RouteConnection(RouteConnectionPoint src, RouteConnectionPoint dst,
			double distance) {
		super();
		this.src = src;
		this.dst = dst;
		this.distance = distance;
	}

	public RouteConnection flip() {
		return new RouteConnection(dst, src, distance);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RouteConnection other = (RouteConnection) obj;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}

}
