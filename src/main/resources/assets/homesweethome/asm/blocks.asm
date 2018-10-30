list n_world_sleeping
ALOAD 0
INVOKEVIRTUAL net/minecraft/world/WorldServer.func_73056_e ()Z

list n_bed
ALOAD 1
GETFIELD net/minecraft/world/World.field_73011_w : Lnet/minecraft/world/WorldProvider;
ALOAD 4
ALOAD 2
INVOKEVIRTUAL net/minecraft/world/WorldProvider.canSleepAt (Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/WorldProvider$WorldSleepResult;

list i_bed
GETSTATIC net/minecraft/world/WorldProvider$WorldSleepResult.ALLOW : Lnet/minecraft/world/WorldProvider$WorldSleepResult;

list i_false
ICONST_0

list i_true
ICONST_1
