package net.devtech.fixedfluids.impl;

import static java.lang.Math.floorDiv;
import static net.devtech.fixedfluids.api.util.FluidUtil.DROPS;

import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.Transaction;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * todo combine empty and filled logic into one
 */
public abstract class AbstractFilledBucketItemParticipant implements Participant.State<Integer> {
	public static final class Bucket extends AbstractFilledBucketItemParticipant {
		public Bucket(Participant<?> inventory, ItemStack original, Fluid fluid) {
			super(inventory, original, fluid);
		}

		@Override
		protected Item empty() {
			return Items.BUCKET;
		}
	}

	private final Participant<?> inventory;
	private final ItemStack original;
	private final Fluid fluid;

	public AbstractFilledBucketItemParticipant(Participant<?> inventory, ItemStack original, Fluid fluid) {
		this.inventory = inventory;
		this.original = original;
		this.fluid = fluid;
	}

	protected abstract Item empty();

	@Override
	public long interact(Transaction transaction, Object type, long amount) {
		if (type != this.fluid) {
			return amount > 0 ? amount : 0;
		}

		// can only be drained from
		if (amount < 0) {
			// emptying the stack
			Integer count = transaction.getOrDefault(this, this.original.getCount());
			// how much we can take out given the number of buckets
			amount = Math.min(floorDiv(-amount, DROPS), count);
			// the amount of empty buckets the inventory can actually take
			amount = this.inventory.interact(transaction, this.empty(), amount);
			transaction.set(this, count - (int)amount);
			return amount * DROPS;
		}

		return amount;
	}

	@Override
	public void onCommit(Integer data) {
		this.original.setCount(data);
	}
}
