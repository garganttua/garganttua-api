package com.garganttua.api.core.security.authentication.loginpassword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.authentication.pin.GGAPIAuthenticatorPin;
import com.garganttua.api.core.security.authentication.pin.GGAPIAuthenticatorPinErrorCounter;
import com.garganttua.api.core.security.authentication.pin.GGAPIPinEntityAuthenticatorHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonLocked;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorCredentialsNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorEnabled;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorLogin;

import java.util.List;

/**
 * Pins down the contract that GGAPILoginPasswordAuthentication.doAuthentication
 * relies on after the 2.0.9 fix: a successful password login must reset the PIN
 * error counter, and a failed one must increment it (and lock the account when
 * the threshold is reached). Without this, mixed PIN+password usage ends up
 * locking the user permanently.
 */
class PinErrorCounterContractTest {

	@GGAPIAuthenticator
	static class PinEquippedUser {
		@GGAPIAuthenticatorLogin
		String login;
		@GGAPIAuthenticatorPin
		String pin;
		@GGAPIAuthenticatorPinErrorCounter(maxErrorNumber = 3)
		int pinErrorCounter;
		@GGAPIAuthenticatorAccountNonExpired
		Boolean accountNonExpired = true;
		@GGAPIAuthenticatorAccountNonLocked
		Boolean accountNonLocked = true;
		@GGAPIAuthenticatorCredentialsNonExpired
		Boolean credentialsNonExpired = true;
		@GGAPIAuthenticatorEnabled
		Boolean enabled = true;
		@GGAPIAuthenticatorAuthorities
		List<String> authorities;
	}

	@GGAPIAuthenticator
	static class PasswordOnlyUser {
		@GGAPIAuthenticatorLogin
		String login;
		@GGAPIAuthenticatorAccountNonExpired
		Boolean accountNonExpired = true;
		@GGAPIAuthenticatorAccountNonLocked
		Boolean accountNonLocked = true;
		@GGAPIAuthenticatorCredentialsNonExpired
		Boolean credentialsNonExpired = true;
		@GGAPIAuthenticatorEnabled
		Boolean enabled = true;
		@GGAPIAuthenticatorAuthorities
		List<String> authorities;
	}

	@Nested
	@DisplayName("On a PIN-equipped authenticator")
	class PinEquipped {

		@Test
		@DisplayName("resetPinErrorNumber zeroes a non-zero counter")
		void reset_zeroes_the_counter() throws GGAPIException {
			PinEquippedUser user = new PinEquippedUser();
			user.pinErrorCounter = 2;

			GGAPIPinEntityAuthenticatorHelper.resetPinErrorNumber(user);

			assertEquals(0, user.pinErrorCounter,
					"reset must set the counter to exactly 0");
		}

		@Test
		@DisplayName("incrementPinErrorNumber bumps the counter by 1 (under threshold)")
		void increment_bumps_under_threshold() throws GGAPIException {
			PinEquippedUser user = new PinEquippedUser();
			user.pinErrorCounter = 0;

			GGAPIPinEntityAuthenticatorHelper.incrementPinErrorNumber(user);

			assertEquals(1, user.pinErrorCounter, "counter must be 1 after first failure");
			assertTrue(user.accountNonLocked, "account must stay unlocked under threshold");
		}

		@Test
		@DisplayName("incrementPinErrorNumber locks the account at the threshold")
		void increment_locks_at_threshold() throws GGAPIException {
			PinEquippedUser user = new PinEquippedUser();
			user.pinErrorCounter = 2; // one short of max (3)

			GGAPIPinEntityAuthenticatorHelper.incrementPinErrorNumber(user);

			assertEquals(3, user.pinErrorCounter, "counter must reach exactly max");
			assertFalse(user.accountNonLocked,
					"account must be locked when counter reaches max — this is what makes the fix necessary");
		}

		@Test
		@DisplayName("reset after a near-lock state un-arms the lock trigger")
		void reset_undoes_near_lock_state() throws GGAPIException {
			PinEquippedUser user = new PinEquippedUser();
			user.pinErrorCounter = 2;

			GGAPIPinEntityAuthenticatorHelper.resetPinErrorNumber(user);

			assertEquals(0, user.pinErrorCounter);
			// Sanity check: a follow-up failure cannot reach the threshold in a single hop.
			GGAPIPinEntityAuthenticatorHelper.incrementPinErrorNumber(user);
			assertEquals(1, user.pinErrorCounter);
			assertTrue(user.accountNonLocked,
					"after a successful password login resets the counter, a single fresh failure must not lock the account");
		}
	}

	@Nested
	@DisplayName("On a password-only (non-PIN) authenticator")
	class PasswordOnly {

		@Test
		@DisplayName("calling reset throws — the catch in GGAPILoginPasswordAuthentication must swallow this")
		void helper_throws_on_non_pin_entity() {
			PasswordOnlyUser user = new PasswordOnlyUser();

			// GGAPILoginPasswordAuthentication wraps this call in try/catch (GGAPIException)
			// — assert here that the wrapped exception type is indeed GGAPIException so the
			// catch keeps working if the helper internals change.
			assertThrows(GGAPIException.class,
					() -> GGAPIPinEntityAuthenticatorHelper.resetPinErrorNumber(user),
					"helper must signal the missing PIN annotations via GGAPIException so the password-side catch block can swallow it cleanly");
		}
	}
}
