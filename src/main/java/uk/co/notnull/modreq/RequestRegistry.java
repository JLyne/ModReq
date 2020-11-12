package uk.co.notnull.modreq;

import org.bukkit.entity.Player;

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
		return makeFuture(() -> plugin.getDataSource().requestExists(id));
	}

	/**
	 * Retrieves the request with the given id, if it exists in storage
	 * @param id The id to check
	 * @return Future completed with the Request if it exists, otherwise null.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> get(int id) {
		return makeFuture(() -> plugin.getDataSource().getRequest(id));
	}

	/**
	 * Elevates the given request
	 * @param request The request to elevate
	 * @return Future completed with the updated Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> elevate(Request request, boolean elevated) {
		return makeFuture(() -> plugin.getDataSource().elevateRequest(request, elevated));
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
	 * @return Future completed with the updated Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> reopen(Request request) {
		return makeFuture(() -> plugin.getDataSource().reopenRequest(request));
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
	 * @param request The request to claim
	 * @return Future completed with the updated Request if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Request> unclaim(Request request) {
		return makeFuture(() -> plugin.getDataSource().unclaim(request));
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
	 * Returns a collection containing all open requests, optionally including elevated requests
	 * @param includeElevated Whether to include elevated requests
	 * @return Future completed with a collection of results if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<RequestCollection> getAllOpen(boolean includeElevated) {
		return makeFuture(() -> plugin.getDataSource().getAllOpenRequests(includeElevated));
	}

	/**
	 * Returns a collection containing the given page open requests, optionally including elevated requests
	 * The results to include will be determined by the page provided, and the configuration option for requests per page
	 * @param includeElevated Whether to include elevated requests
	 * @return Future completed with a collection of results if successful. The collection may be empty if there aren't
	 *         sufficient open requests to reach the requested page.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<RequestCollection> getOpen(int page, boolean includeElevated) {
		return makeFuture(() -> plugin.getDataSource().getOpenRequests(page, includeElevated));
	}

	/**
	 * Returns a collection containing all open requests created by the given player.
	 * @param player The player to retrieve the created requests for
	 * @return Future completed with a collection of results if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<RequestCollection> getOpen(Player player) {
		return makeFuture(() -> plugin.getDataSource().getOpenRequests(player));
	}

	/**
	 * Returns the number of currently open requests, optionally including elevated requests.
	 * @param includeElevated Whether to include elevated requests
	 * @return Future completed with the request count if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Integer> getOpenCount(boolean includeElevated) {
		return makeFuture(() -> plugin.getDataSource().getOpenRequestCount(includeElevated));
	}

	/**
	 * Returns the number of currently open requests, created by the given player.
	 * @param player The player to count the created requests for
	 * @return Future completed with the request count if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Integer> getOpenCount(Player player) {
		return makeFuture(() -> plugin.getDataSource().getOpenRequestCount(player));
	}

	/**
	 * Retrieves the notes added to the given request
	 * @param request The request to retrieve the notes for
	 * @return Future completed with a collection of notes if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<List<Note>> getNotes(Request request) {
		return makeFuture(() -> plugin.getDataSource().getNotesForRequest(request));
	}

	/**
	 * Adds a note by the given player, to the given request
	 * @param request The request to add the note to
	 * @param player The player to create the note for
	 * @param note The note
	 * @return Future completed with the created note if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Note> addNote(Request request, Player player, String note) {
		return makeFuture(() -> plugin.getDataSource().addNoteToRequest(request, player, note));
	}

	/**
	 * Removes the given note from storage
	 * @param note The note to remove
	 * @return Future completed a boolean indicating whether the removal was successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<Boolean> removeNote(Note note) {
		return makeFuture(() -> plugin.getDataSource().removeNote(note));
	}

	/**
	 * Returns a collection containing all closed requests created by the given player, with responses they have not yet seen..
	 * @param player The player to retrieve the unseen requests for
	 * @param markSeen If true, the retrieved requests will also be marked as seen in storage.
	 * @return Future completed with the request count if successful.
	 *         Future completed exceptionally if a storage error occurs.
	 */
	public CompletableFuture<RequestCollection> getUnseenClosed(Player player, boolean markSeen) {
		return makeFuture(() -> {
			RequestCollection requests = plugin.getDataSource().getUnseenClosedRequests(player);

			if(markSeen) {
				return plugin.getDataSource().markRequestsAsSeen(requests);
			}

			return requests;
		});
	}
}
