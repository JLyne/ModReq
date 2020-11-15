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

	public RequestQuery() {

	}

	public static RequestQuery open() {
		return new RequestQuery().status(RequestStatus.OPEN);
	}

	public static RequestQuery closed() {
		return new RequestQuery().status(RequestStatus.CLOSED_SEEN, RequestStatus.CLOSED_UNSEEN);
	}

	public static RequestQuery unseen() {
		return new RequestQuery().status(RequestStatus.CLOSED_UNSEEN);
	}

	public static RequestQuery seen() {
		return new RequestQuery().status(RequestStatus.CLOSED_SEEN);
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

	public boolean hasIds() {
		return !ids.isEmpty();
	}

	public boolean hasCreators() {
		return !creators.isEmpty();
	}

	public boolean hasStatuses() {
		return !statuses.isEmpty();
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
