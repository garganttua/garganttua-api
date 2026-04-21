package com.garganttua.api.commons.operation;

public record OperationPath(String path) {

	public String domain() {
		if (path == null || path.isEmpty()) return null;
		String trimmed = path.startsWith("/") ? path.substring(1) : path;
		int slashIndex = trimmed.indexOf('/');
		return slashIndex >= 0 ? trimmed.substring(0, slashIndex) : trimmed;
	}

	public String suffix() {
		if (path == null || path.isEmpty()) return null;
		String trimmed = path.startsWith("/") ? path.substring(1) : path;
		int slashIndex = trimmed.indexOf('/');
		return slashIndex >= 0 ? trimmed.substring(slashIndex + 1) : null;
	}

	@Override
	public String toString() {
		return path;
	}
}
