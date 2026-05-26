package com.garganttua.api.core;

import java.io.PrintStream;

import com.garganttua.core.bootstrap.banner.IBanner;

/**
 * Default Garganttua API banner.
 *
 * <p>Mirrors {@code com.garganttua.core.bootstrap.banner.GarganttuaBanner} but
 * carries the {@code Garganttua API} tagline + the api module's own version
 * (read from {@link GarganttuaApiVersion}). Installed automatically on the
 * private bootstrap created by {@code ApiBuilder.builder()}; the user can
 * override via {@code apiBuilder.bootstrap().withBanner(...)} or disable
 * entirely with {@code .withBannerMode(BannerMode.OFF)}.
 *
 * @since 3.0.0-ALPHA01
 */
public class GarganttuaApiBanner implements IBanner {

	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_BOLD = "\u001B[1m";

	private static final String[] BANNER_LINES = {
			"",
			"   ██████╗  █████╗ ██████╗  ██████╗  █████╗ ███╗   ██╗████████╗████████╗██╗   ██╗ █████╗ ",
			"  ██╔════╝ ██╔══██╗██╔══██╗██╔════╝ ██╔══██╗████╗  ██║╚══██╔══╝╚══██╔══╝██║   ██║██╔══██╗",
			"  ██║  ███╗███████║██████╔╝██║  ███╗███████║██╔██╗ ██║   ██║      ██║   ██║   ██║███████║",
			"  ██║   ██║██╔══██║██╔══██╗██║   ██║██╔══██║██║╚██╗██║   ██║      ██║   ██║   ██║██╔══██║",
			"  ╚██████╔╝██║  ██║██║  ██║╚██████╔╝██║  ██║██║ ╚████║   ██║      ██║   ╚██████╔╝██║  ██║",
			"   ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝   ╚═╝      ╚═╝    ╚═════╝ ╚═╝  ╚═╝",
			""
	};

	private static final String TAGLINE = "  :: Garganttua API ::";

	private final String version;
	private final boolean useColors;

	public GarganttuaApiBanner() {
		this(GarganttuaApiVersion.getVersion(), true);
	}

	public GarganttuaApiBanner(String version) {
		this(version, true);
	}

	public GarganttuaApiBanner(String version, boolean useColors) {
		this.version = version != null ? version : "UNKNOWN";
		this.useColors = useColors;
	}

	@Override
	public void print(PrintStream out) {
		for (String line : BANNER_LINES) {
			if (useColors) {
				out.println(ANSI_CYAN + ANSI_BOLD + line + ANSI_RESET);
			} else {
				out.println(line);
			}
		}
		String versionPadding = createPadding(TAGLINE, version);
		if (useColors) {
			out.println(ANSI_GREEN + TAGLINE + versionPadding + ANSI_YELLOW + "(" + version + ")" + ANSI_RESET);
		} else {
			out.println(TAGLINE + versionPadding + "(" + version + ")");
		}
		out.println();
	}

	private String createPadding(String tagline, String version) {
		int bannerWidth = BANNER_LINES[1].length();
		int contentWidth = tagline.length() + version.length() + 2;
		int padding = bannerWidth - contentWidth;
		if (padding < 1) {
			padding = 1;
		}
		return " ".repeat(padding);
	}
}
