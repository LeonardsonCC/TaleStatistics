package br.com.leonardson.listeners;

import br.com.leonardson.Main;
import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PlayerItemStatsListener {
    private final Main plugin;
    private final DatabaseManager database;

    public PlayerItemStatsListener(@Nonnull Main plugin, @Nonnull DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void register() {
        plugin.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, this::onInventoryChange);
        plugin.getLogger().at(Level.INFO).log("Item stats listeners registered successfully");
    }

    private void onInventoryChange(@Nonnull LivingEntityInventoryChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        int added = countAddedItems(event.getTransaction());
        if (added <= 0) {
            return;
        }

        database.incrementStat(player.getUuid().toString(), "items_picked_up", added);
        plugin.getLogger().at(Level.FINE).log("Items picked up recorded for player " + player.getUuid());
    }

    private int countAddedItems(Transaction transaction) {
        if (transaction == null || !transaction.succeeded()) {
            return 0;
        }

        if (transaction instanceof MoveTransaction<?>) {
            return 0;
        }

        if (transaction instanceof ListTransaction<?> listTransaction) {
            int total = 0;
            for (Transaction child : listTransaction.getList()) {
                total += countAddedItems(child);
            }
            return total;
        }

        if (transaction instanceof ItemStackTransaction itemStackTransaction) {
            int total = 0;
            for (ItemStackSlotTransaction slotTransaction : itemStackTransaction.getSlotTransactions()) {
                if (!slotTransaction.succeeded()) {
                    continue;
                }
                total += computeAdded(slotTransaction.getSlotBefore(), slotTransaction.getSlotAfter());
            }
            return total;
        }

        if (transaction instanceof SlotTransaction slotTransaction) {
            if (!slotTransaction.succeeded()) {
                return 0;
            }
            return computeAdded(slotTransaction.getSlotBefore(), slotTransaction.getSlotAfter());
        }

        return 0;
    }

    private int computeAdded(ItemStack before, ItemStack after) {
        if (after == null || after.isEmpty()) {
            return 0;
        }

        if (before == null || before.isEmpty()) {
            return after.getQuantity();
        }

        if (!before.getItemId().equals(after.getItemId())) {
            return after.getQuantity();
        }

        int diff = after.getQuantity() - before.getQuantity();
        return Math.max(diff, 0);
    }

}
