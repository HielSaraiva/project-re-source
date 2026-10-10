package edu.br.resource.resourcesystem.presentation;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("sharedAssets")
public class SharedAssetResolver {
    private static final Map<String, String> PATHS =
            Map.ofEntries(
                    Map.entry(
                            "/images/donor/donation-completed/calendar.svg",
                            "/images/shared/icons/calendar.svg"),
                    Map.entry(
                            "/images/donor/donation-completed/details-divider.svg",
                            "/images/shared/icons/line.svg"),
                    Map.entry(
                            "/images/donor/donation-completed/home.svg",
                            "/images/shared/icons/home.svg"),
                    Map.entry(
                            "/images/donor/donation-completed/info.svg",
                            "/images/shared/icons/info.svg"),
                    Map.entry(
                            "/images/donor/donation-completed/mail.svg",
                            "/images/shared/icons/mail.svg"),
                    Map.entry(
                            "/images/donor/donation-completed/tag.svg",
                            "/images/shared/icons/tag.svg"),
                    Map.entry(
                            "/images/donor/donation-completed/user.svg",
                            "/images/shared/icons/user.svg"),
                    Map.entry(
                            "/images/donor/donation-status/calendar.svg",
                            "/images/shared/icons/calendar.svg"),
                    Map.entry(
                            "/images/donor/donation-status/file-text.svg",
                            "/images/shared/icons/file-text.svg"),
                    Map.entry(
                            "/images/donor/donation-status/home.svg",
                            "/images/shared/icons/home.svg"),
                    Map.entry(
                            "/images/donor/donation-status/info.svg",
                            "/images/shared/icons/info.svg"),
                    Map.entry(
                            "/images/donor/donation-status/line.svg",
                            "/images/shared/icons/line.svg"),
                    Map.entry(
                            "/images/donor/donation-status/mail.svg",
                            "/images/shared/icons/mail.svg"),
                    Map.entry(
                            "/images/donor/donation-status/tag.svg",
                            "/images/shared/icons/tag.svg"),
                    Map.entry(
                            "/images/donor/donation-status/user.svg",
                            "/images/shared/icons/user.svg"),
                    Map.entry(
                            "/images/donor/shipping/file-text.svg",
                            "/images/shared/icons/file-text.svg"),
                    Map.entry(
                            "/images/donor/shipping/package.svg",
                            "/images/shared/categories/package.svg"),
                    Map.entry("/images/donor/shipping/user.svg", "/images/shared/icons/user.svg"));

    public String resolve(String path) {
        return PATHS.getOrDefault(path, path);
    }
}
