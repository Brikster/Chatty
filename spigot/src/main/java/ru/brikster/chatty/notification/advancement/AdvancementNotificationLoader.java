package ru.brikster.chatty.notification.advancement;

import com.google.inject.Injector;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.advancement.Advancement;
import ru.brikster.chatty.config.file.NotificationsConfig;
import ru.brikster.chatty.config.file.NotificationsConfig.AdvancementNotificationsConfig.AdvancementNotificationChannelConfig;
import ru.brikster.chatty.config.file.NotificationsConfig.AdvancementNotificationsConfig.AdvancementNotificationChannelConfig.AdvancementNotificationMessageConfig;
import ru.brikster.chatty.notification.AdvancementNotification;
import ru.brikster.chatty.notification.NotificationTicker;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class AdvancementNotificationLoader {

    public void load(NotificationTicker ticker, NotificationsConfig config,
                     BukkitAudiences audiences, Injector injector) {
        AdvancementToaster toaster = injector.getInstance(AdvancementToaster.class);

        if (!config.getAdvancements().isEnable()) {
            toaster.removeStale();
            return;
        }

        config.getAdvancements().getLists().forEach((channelId, channelConfig) -> {
            List<Advancement> toasts = register(toaster, channelId, channelConfig);
            if (toasts.isEmpty()) {
                return;
            }
            ticker.addNotification(new AdvancementNotification(
                    channelId, channelConfig.getPeriod(), toasts,
                    channelConfig.isPermissionRequired(), channelConfig.isRandomOrder(),
                    channelConfig.isPlaySound() ? channelConfig.getSound() : null,
                    audiences, toaster));
        });

        toaster.removeStale();
    }

    private List<Advancement> register(AdvancementToaster toaster, String channelId,
                                       AdvancementNotificationChannelConfig channelConfig) {
        List<Advancement> toasts = new ArrayList<>();
        for (int index = 0; index < channelConfig.getMessages().size(); index++) {
            AdvancementNotificationMessageConfig messageConfig = channelConfig.getMessages().get(index);
            Advancement toast = toaster.register(channelId + "_" + index,
                    messageConfig.getTitle(), messageConfig.getSubtitle(),
                    messageConfig.getIcon(), messageConfig.getFrame());
            if (toast != null) {
                toasts.add(toast);
            }
        }
        return toasts;
    }

}
