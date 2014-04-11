package app.velor.storage.vo;

import app.velor.storage.mapper.DatabaseMapping;

public class RouteConnectionPoint {
	@DatabaseMapping(name = "first_id")
	private long route_id;

	@DatabaseMapping(name = "first_ord")
	private int ordinality;

	private int hash;

	public RouteConnectionPoint() {
		super();
	}

	public RouteConnectionPoint(long route_id, int ordinality) {
		super();
		this.route_id = route_id;
		this.ordinality = ordinality;
		// precomputeHash();
	}

	private void precomputeHash() {
		// precompute hashcode
	}

	public long getRouteId() {
		return route_id;
	}

	public int getOrdinality() {
		return ordinality;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		hash = 1;
		hash = prime * hash + ordinality;
		hash = prime * hash + (int) (route_id ^ (route_id >>> 32));
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RouteConnectionPoint other = (RouteConnectionPoint) obj;
		if (ordinality != other.ordinality)
			return false;
		if (route_id != other.route_id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RouteConnectionPoint [route_id=" + route_id + ", ordinality="
				+ ordinality + "]";
	}

}
