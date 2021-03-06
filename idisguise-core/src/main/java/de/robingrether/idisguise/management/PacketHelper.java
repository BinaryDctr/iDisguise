package de.robingrether.idisguise.management;

import static de.robingrether.idisguise.management.Reflection.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.robingrether.idisguise.iDisguise;
import de.robingrether.idisguise.disguise.AgeableDisguise;
import de.robingrether.idisguise.disguise.ArmorStandDisguise;
import de.robingrether.idisguise.disguise.ChestedHorseDisguise;
import de.robingrether.idisguise.disguise.CreeperDisguise;
import de.robingrether.idisguise.disguise.Disguise;
import de.robingrether.idisguise.disguise.DisguiseType;
import de.robingrether.idisguise.disguise.EndermanDisguise;
import de.robingrether.idisguise.disguise.FallingBlockDisguise;
import de.robingrether.idisguise.disguise.HorseDisguise;
import de.robingrether.idisguise.disguise.ItemDisguise;
import de.robingrether.idisguise.disguise.LlamaDisguise;
import de.robingrether.idisguise.disguise.MinecartDisguise;
import de.robingrether.idisguise.disguise.MobDisguise;
import de.robingrether.idisguise.disguise.ObjectDisguise;
import de.robingrether.idisguise.disguise.OcelotDisguise;
import de.robingrether.idisguise.disguise.ParrotDisguise;
import de.robingrether.idisguise.disguise.PigDisguise;
import de.robingrether.idisguise.disguise.PlayerDisguise;
import de.robingrether.idisguise.disguise.RabbitDisguise;
import de.robingrether.idisguise.disguise.SheepDisguise;
import de.robingrether.idisguise.disguise.SizedDisguise;
import de.robingrether.idisguise.disguise.StyledHorseDisguise;
import de.robingrether.idisguise.disguise.VillagerDisguise;
import de.robingrether.idisguise.disguise.WolfDisguise;
import de.robingrether.idisguise.disguise.ZombieVillagerDisguise;
import de.robingrether.idisguise.disguise.LlamaDisguise.SaddleColor;

public class PacketHelper {
	
	private static PacketHelper instance;
	
	public static PacketHelper getInstance() {
		return instance;
	}
	
	static void setInstance(PacketHelper instance) {
		PacketHelper.instance = instance;
	}
	
	private final boolean[] attributes = new boolean[2];
	/*
	 * attributes[0] -> show original player name for mob disguise (1.5+) and object disguise (1.8+)
	 * 
	 * attributes[1] -> modify player list (tab key list)
	 * 
	 */
	
	public Object[] getPackets(Player player) {
		try {
			Disguise disguise = DisguiseManager.getInstance().getDisguise(player);
			if(disguise == null) return null;
			
			Object entityPlayer = CraftPlayer_getHandle.invoke(player);
			DisguiseType type = disguise.getType();
			List<Object> packets = new ArrayList<Object>();
			
			if(disguise instanceof MobDisguise) {
				MobDisguise mobDisguise = (MobDisguise)disguise;
				Object entity = null;
				if(VersionHelper.require1_11()) {
					entity = Class.forName(VersionHelper.getNMSPackage() + "." + type.getNMSClass()).getConstructor(World).newInstance(Entity_world.get(entityPlayer));
				} else {
					entity = Class.forName(VersionHelper.getNMSPackage() + "." + type.getNMSClass().replaceAll("(Guardian|Horse|Skeleton|Zombie)(Elder|Donkey|Mule|Skeleton|Zombie|Wither|Stray|Villager|Husk)", "$1")).getConstructor(World).newInstance(Entity_world.get(entityPlayer));
					switch(type) {
						case ELDER_GUARDIAN:
							EntityGuardian_setElder.invoke(entity, true);
							break;
						case DONKEY:
						case MULE:
						case SKELETAL_HORSE:
						case UNDEAD_HORSE:
							if(VersionHelper.require1_9()) {
								EntityHorse_setType.invoke(entity, EnumHorseType_fromIndex.invoke(null, ((HorseDisguise)mobDisguise).getVariant()));
							} else {
								EntityHorse_setType.invoke(entity, ((HorseDisguise)mobDisguise).getVariant());
							}
							break;
						case WITHER_SKELETON:
							if(VersionHelper.require1_10()) {
								EntitySkeleton_setSkeletonType.invoke(entity, EnumSkeletonType_fromIndex.invoke(null, 1));
							} else {	
								EntitySkeleton_setSkeletonType.invoke(entity, 1);
							}
							break;
						case STRAY:
							if(VersionHelper.require1_10()) {
								EntitySkeleton_setSkeletonType.invoke(entity, EnumSkeletonType_fromIndex.invoke(null, 2));
							} else {	
								EntitySkeleton_setSkeletonType.invoke(entity, 2);
							}
							break;
						case HUSK:
							EntityZombie_setVillagerType.invoke(entity, EnumZombieType_fromIndex.invoke(null, 6));
							break;
						case ZOMBIE_VILLAGER:
							if(!VersionHelper.require1_9()) {
								EntityZombie_setVillager.invoke(entity, true);
							}
							break;
						default: break;
					}
				}
				if(mobDisguise.getCustomName() != null && !mobDisguise.getCustomName().isEmpty()) {
					Entity_setCustomName.invoke(entity, mobDisguise.getCustomName());
					Entity_setCustomNameVisible.invoke(entity, mobDisguise.isCustomNameVisible());
				}
				
				if(mobDisguise instanceof AgeableDisguise) {
					if(!((AgeableDisguise)mobDisguise).isAdult()) {	
						if(EntityAgeable.isInstance(entity)) {
							EntityAgeable_setAge.invoke(entity, -24000);
						} else if(EntityZombie.isInstance(entity)) {
							EntityZombie_setBaby.invoke(entity, true);
						}
					}
					
					if(mobDisguise instanceof HorseDisguise) {
						HorseDisguise horseDisguise = (HorseDisguise)mobDisguise;
						Object inventoryChest = VersionHelper.require1_11() ? EntityHorseAbstract_inventoryChest.get(entity) : EntityHorse_inventoryChest.get(entity);
						InventorySubcontainer_setItem.invoke(inventoryChest, 0, CraftItemStack_asNMSCopy.invoke(null, horseDisguise.isSaddled() ? new ItemStack(Material.SADDLE) : null));
						InventorySubcontainer_setItem.invoke(inventoryChest, 1, CraftItemStack_asNMSCopy.invoke(null, horseDisguise.getArmor().getItem()));
						if(horseDisguise instanceof StyledHorseDisguise) {
							EntityHorse_setVariant.invoke(entity, ((StyledHorseDisguise)horseDisguise).getColor().ordinal() & 0xFF | ((StyledHorseDisguise)horseDisguise).getStyle().ordinal() << 8);
						} else if(horseDisguise instanceof ChestedHorseDisguise) {
							if(VersionHelper.require1_11()) {
								EntityHorseChestedAbstract_setCarryingChest.invoke(entity, ((ChestedHorseDisguise)horseDisguise).hasChest());
							} else {
								EntityHorse_setHasChest.invoke(entity, ((ChestedHorseDisguise)horseDisguise).hasChest());
							}
						}
					} else if(mobDisguise instanceof LlamaDisguise) {
						LlamaDisguise llamaDisguise = (LlamaDisguise)mobDisguise;
						EntityLlama_setVariant.invoke(entity, llamaDisguise.getColor().ordinal());
						Object inventoryChest = EntityHorseAbstract_inventoryChest.get(entity);
						InventorySubcontainer_setItem.invoke(inventoryChest, 1, CraftItemStack_asNMSCopy.invoke(null, llamaDisguise.getSaddle().equals(SaddleColor.NOT_SADDLED) ? null : new ItemStack(Material.CARPET, 1, (short)llamaDisguise.getSaddle().ordinal())));
						EntityHorseChestedAbstract_setCarryingChest.invoke(entity, llamaDisguise.hasChest());
					} else if(mobDisguise instanceof OcelotDisguise) {
						EntityOcelot_setCatType.invoke(entity, ((OcelotDisguise)mobDisguise).getCatType().getId());
					} else if(mobDisguise instanceof PigDisguise) {
						EntityPig_setSaddle.invoke(entity, ((PigDisguise)mobDisguise).isSaddled());
					} else if(mobDisguise instanceof RabbitDisguise) {
						EntityRabbit_setRabbitType.invoke(entity, ((RabbitDisguise)mobDisguise).getRabbitType().getId());
					} else if(mobDisguise instanceof SheepDisguise) {
						EntitySheep_setColor.invoke(entity, EnumColor_fromColorIndex.invoke(null, ((SheepDisguise)mobDisguise).getColor().getWoolData()));
					} else if(mobDisguise instanceof VillagerDisguise) {
						EntityVillager_setProfession.invoke(entity, ((VillagerDisguise)mobDisguise).getProfession().ordinal());
					} else if(mobDisguise instanceof WolfDisguise) {
						WolfDisguise wolfDisguise = (WolfDisguise)mobDisguise;
						EntityWolf_setCollarColor.invoke(entity, EnumColor_fromColorIndex.invoke(null, wolfDisguise.getCollarColor().getWoolData()));
						EntityWolf_setTamed.invoke(entity, wolfDisguise.isTamed());
						EntityWolf_setAngry.invoke(entity, wolfDisguise.isAngry());
					} else if(mobDisguise instanceof ZombieVillagerDisguise) {
						if(VersionHelper.require1_11()) {
							EntityZombieVillager_setProfession.invoke(entity, ((ZombieVillagerDisguise)mobDisguise).getProfession().ordinal());
						} else if(VersionHelper.require1_10()) {
							EntityZombie_setVillagerType.invoke(entity, EnumZombieType_fromIndex.invoke(null, ((ZombieVillagerDisguise)mobDisguise).getProfession().ordinal() + 1));
						} else if(VersionHelper.require1_9()) {
							EntityZombie_setVillagerType.invoke(entity, ((ZombieVillagerDisguise)mobDisguise).getProfession().ordinal());
						}
					}
				} else if(mobDisguise instanceof CreeperDisguise) {
					EntityCreeper_setPowered.invoke(entity, ((CreeperDisguise)mobDisguise).isPowered());
				} else if(mobDisguise instanceof EndermanDisguise) {
					EndermanDisguise endermanDisguise = (EndermanDisguise)mobDisguise;
					EntityEnderman_setCarried.invoke(entity, Block_fromLegacyData.invoke(Block_getById.invoke(null, endermanDisguise.getBlockInHand().getId()), endermanDisguise.getBlockInHandData()));
				} else if(mobDisguise instanceof ParrotDisguise) {
					EntityParrot_setVariant.invoke(entity, ((ParrotDisguise)mobDisguise).getVariant().ordinal());
				} else if(mobDisguise instanceof SizedDisguise) {
					if(VersionHelper.require1_11()) {
						EntitySlime_setSize.invoke(entity, ((SizedDisguise)mobDisguise).getSize(), false);
					} else {
						EntitySlime_setSize.invoke(entity, ((SizedDisguise)mobDisguise).getSize());
					}
				}
				
				if(EntityBat.isInstance(entity)) {
					EntityBat_setAsleep.invoke(entity, false);
				}
				
				Location location = player.getLocation();
				Entity_setLocation.invoke(entity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
				Entity_setEntityId.invoke(entity, player.getEntityId());
				if(attributes[0]) {
					Entity_setCustomName.invoke(entity, player.getName());
					Entity_setCustomNameVisible.invoke(entity, true);
				}
				packets.add(PacketPlayOutSpawnEntityLiving_new.newInstance(entity));
			} else if(disguise instanceof PlayerDisguise) {
				packets.add(PacketPlayOutNamedEntitySpawn_new.newInstance(entityPlayer));
				// don't modify anything here, skin is applied via player list item packet
			} else if(disguise instanceof ObjectDisguise) {
				ObjectDisguise objectDisguise = (ObjectDisguise)disguise;
				Object entity = Class.forName(VersionHelper.getNMSPackage() + "." + type.getNMSClass()).getConstructor(World).newInstance(Entity_world.get(entityPlayer));
				Location location = player.getLocation();
				Entity_setLocation.invoke(entity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
				Entity_setEntityId.invoke(entity, player.getEntityId());
				if(attributes[0]) {
					Entity_setCustomName.invoke(entity, player.getName());
					Entity_setCustomNameVisible.invoke(entity, true);
				} else if(objectDisguise.getCustomName() != null && !objectDisguise.getCustomName().isEmpty()) {
					Entity_setCustomName.invoke(entity, objectDisguise.getCustomName());
					Entity_setCustomNameVisible.invoke(entity, objectDisguise.isCustomNameVisible());
				}
				if(EntityFallingBlock.isInstance(entity)) {
					packets.add(PacketPlayOutSpawnEntity_new.newInstance(entity, objectDisguise.getTypeId(), objectDisguise instanceof FallingBlockDisguise ? ((FallingBlockDisguise)objectDisguise).getMaterial().getId() | (((FallingBlockDisguise)objectDisguise).getData() << 12) : 1));
				} else if(EntityItem.isInstance(entity)) {
					if(objectDisguise instanceof ItemDisguise) {
						ItemDisguise itemDisguise = (ItemDisguise)objectDisguise;
						EntityItem_setItemStack.invoke(entity, CraftItemStack_asNMSCopy.invoke(null, itemDisguise.getItemStack()));
					}
					packets.add(PacketPlayOutSpawnEntity_new.newInstance(entity, objectDisguise.getTypeId(), 0));
					packets.add(PacketPlayOutEntityMetadata_new_full.newInstance(player.getEntityId(), Entity_getDataWatcher.invoke(entity), true));
				} else if(EntityMinecartAbstract.isInstance(entity)) {
					if(objectDisguise instanceof MinecartDisguise) {
						MinecartDisguise minecartDisguise = (MinecartDisguise)objectDisguise;
						EntityMinecartAbstract_setDisplayBlock.invoke(entity, Block_fromLegacyData.invoke(Block_getById.invoke(null, minecartDisguise.getDisplayedBlock().getId()), minecartDisguise.getDisplayedBlockData()));
					}
					packets.add(PacketPlayOutSpawnEntity_new.newInstance(entity, objectDisguise.getTypeId(), 0));
					packets.add(PacketPlayOutEntityMetadata_new_full.newInstance(player.getEntityId(), Entity_getDataWatcher.invoke(entity), true));
				} else if(EntityArmorStand.isInstance(entity)) {
					if(objectDisguise instanceof ArmorStandDisguise) {
						EntityArmorStand_setArms.invoke(entity, ((ArmorStandDisguise)objectDisguise).getShowArms());
					}
					packets.add(PacketPlayOutSpawnEntity_new.newInstance(entity, objectDisguise.getTypeId(), 0));
					packets.add(PacketPlayOutEntityMetadata_new_full.newInstance(player.getEntityId(), Entity_getDataWatcher.invoke(entity), true));
				} else {
					packets.add(PacketPlayOutSpawnEntity_new.newInstance(entity, objectDisguise.getTypeId(), 0));
				}
			}
			return packets.toArray(new Object[0]);
		} catch(Exception e) {
			if(VersionHelper.debug()) {
				iDisguise.getInstance().getLogger().log(Level.SEVERE, "Cannot construct the required packet.", e);
			}
		}
		return new Object[0];
	}
	
	public Object getPlayerInfo(OfflinePlayer offlinePlayer, Object context, int ping, Object gamemode, Object displayName) {
		Disguise disguise = DisguiseManager.getInstance().getDisguise(offlinePlayer);
		try {
			if(disguise == null) {
				return PlayerInfoData_new.newInstance(context, offlinePlayer.isOnline() ? CraftPlayer_getProfile.invoke(offlinePlayer) : CraftOfflinePlayer_getProfile.invoke(offlinePlayer), ping, gamemode, displayName);
			} else if(disguise instanceof PlayerDisguise) {
				return PlayerInfoData_new.newInstance(context, PlayerHelper.getInstance().getGameProfile(offlinePlayer.getUniqueId(), ((PlayerDisguise)disguise).getSkinName(), ((PlayerDisguise)disguise).getDisplayName()), ping, gamemode, attributes[1] ? Array.get(CraftChatMessage_fromString.invoke(null, ((PlayerDisguise)disguise).getDisplayName()), 0) : displayName != null ? displayName : Array.get(CraftChatMessage_fromString.invoke(null, offlinePlayer.isOnline() ? offlinePlayer.getPlayer().getPlayerListName() : offlinePlayer.getName()), 0));
			} else if(!attributes[1]) {
				return PlayerInfoData_new.newInstance(context, offlinePlayer.isOnline() ? CraftPlayer_getProfile.invoke(offlinePlayer) : CraftOfflinePlayer_getProfile.invoke(offlinePlayer), ping, gamemode, displayName);
			}
		} catch(Exception e) {
			if(VersionHelper.debug()) {
				iDisguise.getInstance().getLogger().log(Level.SEVERE, "Cannot construct the required player info.", e);
			}
		}
		return null;
	}
	
	public int getMetadataId(Object metadataItem) {
		try {
			if(VersionHelper.require1_9()) {
				return (Integer)DataWatcherObject_getId.invoke(DataWatcherItem_dataWatcherObject.get(metadataItem));
			} else {
				return (Integer)DataWatcherObject_getId.invoke(metadataItem);
			}
		} catch(Exception e) {
			if(VersionHelper.debug()) {
				iDisguise.getInstance().getLogger().log(Level.SEVERE, "Cannot retrieve the required metadata id.", e);
			}
		}
		return 127;
	}
	
	public String soundEffectToString(Object soundEffect) {
		if(VersionHelper.require1_9()) {
			try {
				return (String)MinecraftKey_getName.invoke(RegistryMaterials_getKey.invoke(SoundEffect_registry.get(null), soundEffect));
			} catch(Exception e) {
				if(VersionHelper.debug()) {
					iDisguise.getInstance().getLogger().log(Level.SEVERE, "Cannot retrieve the required sound effect name.", e);
				}
			}
		} else {
			return (String)soundEffect;
		}
		return null;
	}
	
	public Object soundEffectFromString(String name) {
		if(VersionHelper.require1_9()) {
			try {
				return RegistryMaterials_getValue.invoke(SoundEffect_registry.get(null), MinecraftKey_new.newInstance(name));
			} catch(Exception e) {
				if(VersionHelper.debug()) {
					iDisguise.getInstance().getLogger().log(Level.SEVERE, "Cannot retrieve the required sound effect.", e);
				}
			}
		} else {
			return name;
		}
		return null;
	}
	
	public Object clonePacket(Object packet) {
		Object clone = null;
		try {
			if(PacketPlayOutPlayerInfo.isInstance(packet)) {
				clone = PacketPlayOutPlayerInfo_new.newInstance();
				PacketPlayOutPlayerInfo_action.set(clone, PacketPlayOutPlayerInfo_action.get(packet));
				PacketPlayOutPlayerInfo_playerInfoList.set(clone, ((ArrayList<?>)PacketPlayOutPlayerInfo_playerInfoList.get(packet)).clone());
			} else if(PacketPlayOutEntityMetadata.isInstance(packet)) {
				clone = PacketPlayOutEntityMetadata_new_empty.newInstance();
				PacketPlayOutEntityMetadata_entityId.setInt(clone, PacketPlayOutEntityMetadata_entityId.getInt(packet));
				PacketPlayOutEntityMetadata_metadataList.set(clone, ((ArrayList<?>)PacketPlayOutEntityMetadata_metadataList.get(packet)).clone());
			} else if(PacketPlayOutEntity.isInstance(packet)) {
				clone = packet.getClass().newInstance();
				PacketPlayOutEntity_entityId.setInt(clone, PacketPlayOutEntity_entityId.getInt(packet));
				PacketPlayOutEntity_deltaX.set(clone, PacketPlayOutEntity_deltaX.get(packet));
				PacketPlayOutEntity_deltaY.set(clone, PacketPlayOutEntity_deltaY.get(packet));
				PacketPlayOutEntity_deltaZ.set(clone, PacketPlayOutEntity_deltaZ.get(packet));
				PacketPlayOutEntity_yaw.setByte(clone, PacketPlayOutEntity_yaw.getByte(packet));
				PacketPlayOutEntity_pitch.setByte(clone, PacketPlayOutEntity_pitch.getByte(packet));
				PacketPlayOutEntity_isOnGround.setBoolean(clone, PacketPlayOutEntity_isOnGround.getBoolean(packet));
			} else if(PacketPlayOutEntityTeleport.isInstance(packet)) {
				clone = PacketPlayOutEntityTeleport_new.newInstance();
				PacketPlayOutEntityTeleport_entityId.setInt(clone, PacketPlayOutEntityTeleport_entityId.getInt(packet));
				PacketPlayOutEntityTeleport_x.set(clone, PacketPlayOutEntityTeleport_x.get(packet));
				PacketPlayOutEntityTeleport_y.set(clone, PacketPlayOutEntityTeleport_y.get(packet));
				PacketPlayOutEntityTeleport_z.set(clone, PacketPlayOutEntityTeleport_z.get(packet));
				PacketPlayOutEntityTeleport_yaw.setByte(clone, PacketPlayOutEntityTeleport_yaw.getByte(packet));
				PacketPlayOutEntityTeleport_pitch.setByte(clone, PacketPlayOutEntityTeleport_pitch.getByte(packet));
				PacketPlayOutEntityTeleport_isOnGround.setBoolean(clone, PacketPlayOutEntityTeleport_isOnGround.getBoolean(packet));
			} else if(PacketPlayOutNamedSoundEffect.isInstance(packet)) {
				clone = PacketPlayOutNamedSoundEffect_new.newInstance();
				PacketPlayOutNamedSoundEffect_soundEffect.set(clone, PacketPlayOutNamedSoundEffect_soundEffect.get(packet));
				if(VersionHelper.require1_9()) {
					PacketPlayOutNamedSoundEffect_soundCategory.set(clone, PacketPlayOutNamedSoundEffect_soundCategory.get(packet));
				}
				PacketPlayOutNamedSoundEffect_x.setInt(clone, PacketPlayOutNamedSoundEffect_x.getInt(packet));
				PacketPlayOutNamedSoundEffect_y.setInt(clone, PacketPlayOutNamedSoundEffect_y.getInt(packet));
				PacketPlayOutNamedSoundEffect_z.setInt(clone, PacketPlayOutNamedSoundEffect_z.getInt(packet));
				PacketPlayOutNamedSoundEffect_volume.setFloat(clone, PacketPlayOutNamedSoundEffect_volume.getFloat(packet));
				PacketPlayOutNamedSoundEffect_pitch.setInt(clone, PacketPlayOutNamedSoundEffect_pitch.getInt(packet));
			} else if(PacketPlayOutScoreboardTeam.isInstance(packet)) {
				clone = PacketPlayOutScoreboardTeam_new.newInstance();
				PacketPlayOutScoreboardTeam_teamName.set(clone, PacketPlayOutScoreboardTeam_teamName.get(packet));
				PacketPlayOutScoreboardTeam_displayName.set(clone, PacketPlayOutScoreboardTeam_displayName.get(packet));
				PacketPlayOutScoreboardTeam_prefix.set(clone, PacketPlayOutScoreboardTeam_prefix.get(packet));
				PacketPlayOutScoreboardTeam_suffix.set(clone, PacketPlayOutScoreboardTeam_suffix.get(packet));
				PacketPlayOutScoreboardTeam_nameTagVisibility.set(clone, PacketPlayOutScoreboardTeam_nameTagVisibility.get(packet));
				if(VersionHelper.require1_9()) {
					PacketPlayOutScoreboardTeam_collisionRule.set(clone, PacketPlayOutScoreboardTeam_collisionRule.get(packet));
				}
				PacketPlayOutScoreboardTeam_color.setInt(clone, PacketPlayOutScoreboardTeam_color.getInt(packet));
				((Collection<String>)PacketPlayOutScoreboardTeam_entries.get(clone)).addAll((Collection<String>)PacketPlayOutScoreboardTeam_entries.get(packet));
				PacketPlayOutScoreboardTeam_action.setInt(clone, PacketPlayOutScoreboardTeam_action.getInt(packet));
				PacketPlayOutScoreboardTeam_friendlyFlags.setInt(clone, PacketPlayOutScoreboardTeam_friendlyFlags.getInt(packet));
			}
		} catch(Exception e) {
			if(VersionHelper.debug()) {
				iDisguise.getInstance().getLogger().log(Level.SEVERE, "Cannot clone the given packet.", e);
			}
		}
		return clone;
	}
	
	public void setAttribute(int index, boolean value) {
		attributes[index] = value;
	}
	
}