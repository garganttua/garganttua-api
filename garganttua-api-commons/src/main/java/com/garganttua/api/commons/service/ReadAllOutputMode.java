package com.garganttua.api.commons.service;

/**
 * Shape of a {@code readAll} response. The enum constant names ARE the wire values read by
 * {@code READ_ALL.gs} ({@code :arg(@0, "mode")}), so {@code name()} maps directly to the script.
 *
 * <ul>
 *   <li>{@link #full} — the complete entity objects (with injection + {@code @AfterGet} hooks);</li>
 *   <li>{@link #uuid} — only the entities' uuids;</li>
 *   <li>{@link #id} — only the entities' ids.</li>
 * </ul>
 */
public enum ReadAllOutputMode {
    full,
    uuid,
    id
}
