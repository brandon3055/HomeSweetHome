list n_world_sleeping
ALOAD 0
INVOKEVIRTUAL net/minecraft/world/WorldServer.func_73056_e ()Z

list n_bed_1
ALOAD 1
GETFIELD net/minecraft/world/World.field_73011_w : Lnet/minecraft/world/WorldProvider;
INVOKEVIRTUAL net/minecraft/world/WorldProvider.func_76567_e ()Z

list n_bed_2
ALOAD 1
ALOAD 2
INVOKEVIRTUAL net/minecraft/world/World.func_180494_b (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;
GETSTATIC net/minecraft/init/Biomes.field_76778_j : Lnet/minecraft/world/biome/Biome;

list r_bed_2
ALOAD 1
ALOAD 2
INVOKEVIRTUAL net/minecraft/world/World.func_180494_b (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;
ACONST_NULL

list i_false
ICONST_0

list i_true
ICONST_1
