package uk.co.notnull.modreq;

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

	public CompletableFuture<Boolean> exists(int id) {
		return makeFuture(() -> plugin.getDataSource().requestExists(id));
	}

	public CompletableFuture<Request> get(int id) {
		return makeFuture(() -> plugin.getDataSource().getRequest(id));
	}

	public CompletableFuture<Boolean> elevate(int id) {
		return makeFuture(() -> plugin.getDataSource().elevateRequest(id));
	}
}
