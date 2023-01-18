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

package uk.co.notnull.modreq;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestQuery {
	private List<Integer> ids = new ArrayList<>();
	private List<UUID> creators = new ArrayList<>();
	private List<RequestStatus> statuses = new ArrayList<>();
	private List<UUID> responders = new ArrayList<>();
	private List<UUID> owners = new ArrayList<>();
	private String search = null;
	private Boolean seenState = null;

	public RequestQuery() {

	}

	public static RequestQuery open() {
		return new RequestQuery().status(RequestStatus.OPEN);
	}

	public static RequestQuery closed() {
		return new RequestQuery().status(RequestStatus.CLOSED);
	}

	public static RequestQuery unseen() {
		return new RequestQuery().seenState(false);
	}

	public static RequestQuery seen() {
		return new RequestQuery().seenState(true);
	}

	public RequestQuery id(int id) {
		ids.add(id);
		return this;
	}

	public RequestQuery ids(Integer ...ids) {
		this.ids.addAll(List.of(ids));
		return this;
	}

	public RequestQuery ids(List<Integer> ids) {
		this.ids.addAll(ids);
		return this;
	}

	public RequestQuery creator(UUID uuid) {
		creators.add(uuid);
		return this;
	}

	public RequestQuery creators(UUID ...uuids) {
		creators.addAll(List.of(uuids));
		return this;
	}

	public RequestQuery creators(List<UUID> uuids) {
		creators.addAll(uuids);
		return this;
	}

	public RequestQuery status(RequestStatus status) {
		statuses.add(status);
		return this;
	}

	public RequestQuery status(RequestStatus ...statuses) {
		this.statuses.addAll(List.of(statuses));
		return this;
	}

	public RequestQuery status(List<RequestStatus> statuses) {
		this.statuses.addAll(statuses);
		return this;
	}

	public RequestQuery seenState(Boolean seenState) {
		this.seenState = seenState;
		return this;
	}

	public RequestQuery responder(UUID uuid) {
		responders.add(uuid);
		return this;
	}

	public RequestQuery responders(UUID ...uuids) {
		responders.addAll(List.of(uuids));
		return this;
	}

	public RequestQuery responders(List<UUID> uuids) {
		responders.addAll(uuids);
		return this;
	}

	public RequestQuery owner(UUID uuid) {
		owners.add(uuid);
		return this;
	}

	public RequestQuery owners(UUID ...uuids) {
		owners.addAll(List.of(uuids));
		return this;
	}

	public RequestQuery owners(List<UUID> uuids) {
		owners.addAll(uuids);
		return this;
	}

	public RequestQuery search(String search) {
		this.search = search;
		return this;
	}

	public List<Integer> getIds() {
		return ids;
	}

	public List<UUID> getCreators() {
		return creators;
	}

	public List<RequestStatus> getStatuses() {
		return statuses;
	}

	public List<UUID> getResponders() {
		return responders;
	}

	public List<UUID> getOwners() {
		return owners;
	}

	public String getSearch() {
		return search;
	}

	public Boolean getSeenState() {
		return seenState;
	}

	public boolean hasIds() {
		return !ids.isEmpty();
	}

	public boolean hasCreators() {
		return !creators.isEmpty();
	}

	public boolean hasStatuses() {
		return !statuses.isEmpty();
	}

	public boolean hasSeenState() {
		return seenState != null;
	}

	public boolean hasResponders() {
		return !responders.isEmpty();
	}

	public boolean hasOwners() {
		return !owners.isEmpty();
	}

	public boolean hasSearch() {
		return search != null;
	}
}
