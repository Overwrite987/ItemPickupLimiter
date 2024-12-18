package ru.overwrite.pickuplimiter.configuration.data;

public record Messages(
        String noPerm,
        String enabled,
        String alreadyEnabled,
        String disabled,
        String alreadyDisabled,
        String blockSuccess,
        String unblockSuccess,
        String alreadyBlocked,
        String notBlocked,
        String incorrectMaterial,
        String list,
        String usage
) {
}
