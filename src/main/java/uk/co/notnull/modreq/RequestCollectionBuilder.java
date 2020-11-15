package uk.co.notnull.modreq;

import java.util.ArrayList;
import java.util.List;

public class RequestCollectionBuilder {
	private List<Request> requests = new ArrayList<>();
	private boolean paginated = false;
	private int offset = 1;
	private int totalResults = 1;

	RequestCollectionBuilder() {}

	public static RequestCollectionBuilder builder() {
		return new RequestCollectionBuilder();
	}

	public RequestCollectionBuilder addRequest(Request request) {
		requests.add(request);

		return this;
	}

	public RequestCollectionBuilder requests(List<Request> requests) {
		this.requests = requests;

		return this;
	}

	public RequestCollectionBuilder paginated(int offset, int totalResults) {
		this.offset = offset;
		this.totalResults = totalResults;
		this.paginated = true;

		return this;
	}

	public RequestCollection build() {
		if(paginated) {
			return new RequestCollection(requests, offset, totalResults);
		} else {
			return new RequestCollection(requests);
		}
	}
}
