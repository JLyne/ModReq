/*
 * ModReq
 * Copyright (C) 2021 James Lyne
 *
 * Based on ModReq 1.2 (https://www.spigotmc.org/resources/modreq.57560/)
 * Copyright (C) 2019 Aladram and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.notnull.modreq.collections;

import uk.co.notnull.modreq.Request;

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
