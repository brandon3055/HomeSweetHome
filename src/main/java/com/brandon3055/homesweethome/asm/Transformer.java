package com.brandon3055.homesweethome.asm;

import codechicken.asm.*;
import codechicken.asm.transformers.MethodReplacer;
import codechicken.asm.transformers.MethodTransformer;
import com.google.common.collect.Iterables;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.*;

import java.util.Map;

/**
 * Created by covers1624 on 6/01/2018.
 */
public class Transformer implements IClassTransformer {

	private final ModularASMTransformer transformer = new ModularASMTransformer("home_sweet_home");

	public Transformer() {
		ObfMapping mapping;
		Map<String, ASMBlock> blocks = ASMReader.loadResource("/assets/homesweethome/asm/blocks.asm");
		mapping = new ObfMapping("net/minecraft/world/WorldServer", "func_72835_b", "()V");
		transformer.add(new MethodReplacer(mapping, blocks.get("n_world_sleeping"), blocks.get("i_false")));
		mapping = new ObfMapping("net/minecraft/entity/player/EntityPlayer", "func_70071_h_", "()V");
		transformer.add(new PlayerUpdateTransformer(mapping));
		mapping = new ObfMapping("net/minecraft/block/BlockBed", "func_180639_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Z");
		transformer.add(new MethodReplacer(mapping, blocks.get("n_bed_1"), blocks.get("i_true")));
		transformer.add(new MethodReplacer(mapping, blocks.get("n_bed_2"), blocks.get("r_bed_2")));
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return transformer.transform(transformedName, basicClass);
	}

	public static class PlayerUpdateTransformer extends MethodTransformer {

		private static ObfMapping isSleeping = new ObfMapping("net/minecraft/entity/player/EntityPlayer", "func_70608_bn", "()Z");
		private static ObfMapping sleepTimer = new ObfMapping("net/minecraft/entity/player/EntityPlayer", "field_71076_b", "I");

		public PlayerUpdateTransformer(ObfMapping method) {
			super(method);
		}

		//Copies all instructions around the section we want to delete.
		@Override
		public void transform(MethodNode mv) {
			InsnListSection list = new InsnListSection(mv.instructions);

			LabelNode begin = null;
			JumpInsnNode firstJump = null;
			for (AbstractInsnNode insn : list) {
				if (insn instanceof MethodInsnNode) {
					MethodInsnNode m_insn = (MethodInsnNode) insn;
					ObfMapping m = isSleeping.toClassloading();
					//Find out first known point.
					if (m.s_owner.equals(m_insn.owner) && m.s_name.equals(m_insn.name) && m.s_desc.equals(m_insn.desc)) {
						//Look up the method instructions for the previous label, that is out start point.
						begin = getPreviousLabel(m_insn);
						//Find the jump node after our method node.
						if (!(m_insn.getNext() instanceof JumpInsnNode)) {
							throw new RuntimeException("Instruction after " + isSleeping + " is not a JumpInsn.\n" + list);
						}
						firstJump = (JumpInsnNode) m_insn.getNext();
						break;
					}
				}
			}
			if (firstJump == null) {
				throw new RuntimeException("Unable to find InvokeVirtual function " + isSleeping.toClassloading() + " in list:\n" + list);
			}
			JumpInsnNode secondJump;
			AbstractInsnNode scratch = firstJump.label;
			while (true) {
				scratch = scratch.getNext();
				if (scratch instanceof FieldInsnNode) {
					//Find out field after the the method's if end label.
					if (sleepTimer.toClassloading().matches((FieldInsnNode) scratch)) {
						//Grab the jump node after the field.
						AbstractInsnNode next = scratch.getNext();
						if (!(next instanceof JumpInsnNode)) {
							throw new RuntimeException("Instruction after " + sleepTimer + " is not a JumpInsn.\n" + list);
						}
						secondJump = (JumpInsnNode) next;
						break;
					}
				} else if (scratch == null) {
					throw new IllegalArgumentException("Fell off node list." + firstJump.label + "\n" + list);
				}
			}
			//The list to copy our instructions to.
			InsnList newList = new InsnList();
			//The copied labels.
			Map<LabelNode, LabelNode> labels = list.cloneLabels();

			//Create the InsnList views for the ranges around our deletion.
			InsnListSection before = new InsnListSection(mv.instructions, list.getFirst(), begin);
			InsnListSection after = new InsnListSection(mv.instructions, secondJump.label.getNext(), list.getLast());
			//Copy all the instructions to our new list.
			for (AbstractInsnNode node : Iterables.concat(before, after)) {
				newList.add(node.clone(labels));
			}
			//Replace the methods instructions.
			mv.instructions.clear();
			mv.instructions.insert(newList);
		}

		private static LabelNode getPreviousLabel(AbstractInsnNode n) {
			AbstractInsnNode node = n;
			while (true) {
				node = node.getPrevious();
				if (node instanceof LabelNode) {
					return (LabelNode) node;
				} else if (node == null) {
					throw new IllegalArgumentException("Fell off node list. " + n);
				}
			}
		}
	}
}
