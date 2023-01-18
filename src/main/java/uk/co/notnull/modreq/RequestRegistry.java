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

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.collections.RequestCollection;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class RequestRegistry {
	private final ModReq plugin;

	public RequestRegistry(ModReq plugin) {
		this.plugin = plugin;
	}

	private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        });
	}

	/**
	 * Checks whether a request with the given id exists in storage
	 * @param id The id to check
	 * @return Future completed with True if the request exists, otherwise false.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Boolean> exists(int id) {
		if(id < 1) {
			throw new IllegalArgumentException("ID cannot be less than 1");
		}

		return makeFuture(() -> plugin.getDataSource().requestExists(id));
	}

	/**
	 * Retrieves the request with the given id, if it exists in storage
	 * @param id The id to check
	 * @return Future completed with the Request if it exists, otherwise null.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> get(int id) {
		if(id < 1) {
			throw new IllegalArgumentException("ID cannot be less than 1");
		}

		return makeFuture(() -> plugin.getDataSource().getRequest(id));
	}

	/**
	 * Elevates the given request
	 * @param request The request to elevate
	 * @param mod The player elevating the request
	 * @return Future completed with the updated Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> elevate(Request request, Player mod, boolean elevated) {
		return makeFuture(() -> plugin.getDataSource().elevateRequest(request, mod, elevated));
	}

	/**
	 * Closes the given request
	 * @param request The request to close
	 * @param mod The player closing the request
	 * @param message The close message
	 * @return Future completed with the updated Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> close(Request request, Player mod, String message) {
		return makeFuture(() ->  plugin.getDataSource().closeRequest(request, mod, message));
	}

	/**
	 * Reopens the given request
	 * @param request The request to reopen
	 * @param mod The player reopening the request
	 * @return Future completed with the updated Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> reopen(Request request, Player mod) {
		return makeFuture(() -> plugin.getDataSource().reopenRequest(request, mod));
	}

	/**
	 * Claims the given request for the given player
	 * @param request The request to claim
	 * @param player The player to claim the request for
	 * @return Future completed with the updated Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> claim(Request request, Player player) {
		return makeFuture(() -> plugin.getDataSource().claim(request, player));
	}

	/**
	 * Removes any existing claim for the given request
	 * @param request The request from which to remove the claim
	 * @param mod The player removing the claim
	 * @return Future completed with the updated Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> unclaim(Request request, Player mod) {
		return makeFuture(() -> plugin.getDataSource().unclaim(request, mod));
	}

	/**
	 * Creates a request for the given player, with the given message.
	 * The player's current location will be used as the request location.
	 * @param player The player to create the request for
	 * @param message The message for the request
	 * @return Future completed with the created Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> create(Player player, String message) {
		return makeFuture(() -> plugin.getDataSource().createRequest(player, message));
	}

	/**
	 * Returns a collection containing all requests which match the given query
	 * @param query The query to match against
	 * @return Future completed with a collection of results if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<RequestCollection> getAll(RequestQuery query) {
		return makeFuture(() -> plugin.getDataSource().getAllRequests(query));
	}

	/**
	 * Returns a collection containing the given page of requests, which match the given query
	 * @param query The query to match against
	 * @param page The page to return. Will return an empty collection with isAfterLastPage() == true,
	 *                if there are not enough results
	 * @return Future completed with a collection of results if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<RequestCollection> get(RequestQuery query, int page) {
		return makeFuture(() -> plugin.getDataSource().getRequests(query, page));
	}

	/**
	 * Returns the number of requests in storage, which match the given query
	 * @param query The query to match against
	 * @return Future completed with the number of matching requests if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Integer> getCount(RequestQuery query) {
		return makeFuture(() -> plugin.getDataSource().getRequestCount(query));
	}

	/**
	 * Retrieves the update history for the given request
	 * @param request The request to retrieve the history for
	 * @return Future completed with a collection of updates if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<List<Update>> getUpdates(Request request) {
		return makeFuture(() -> plugin.getDataSource().getUpdatesForRequest(request));
	}

	/**
	 * Adds a comment by the given player, to the given request
	 * @param request The request to add the note to
	 * @param player The player to create the note for
	 * @param isPublic Whether the comment can be seen by the request creator
	 * @param comment The comment
	 * @return Future completed with the created note if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Update> addComment(Request request, Player player, boolean isPublic, String comment) {
		return makeFuture(() -> plugin.getDataSource().addCommentToRequest(request, player, isPublic, comment));
	}

	/**
	 * Removes the given note from storage
	 * @param update The note to remove
	 * @return Future completed a boolean indicating whether the removal was successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Boolean> removeComment(Update update) {
		return makeFuture(() -> plugin.getDataSource().removeComment(update));
	}

	/**
	 * Returns a collection containing all closed requests created by the given player, with responses they have not yet seen..
	 * @param player The player to retrieve the unseen requests for
	 * @param markSeen If true, the retrieved requests will also be marked as seen in storage.
	 * @return Future completed with the request count if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<RequestCollection> getUnseen(Player player, boolean markSeen) {
		return makeFuture(() -> {
			RequestQuery query = RequestQuery.unseen().creator(player.getUniqueId());
			RequestCollection requests = plugin.getDataSource().getAllRequests(query);

			if(markSeen) {
				return plugin.getDataSource().markRequestsAsSeen(requests);
			}

			return requests;
		});
	}
}
