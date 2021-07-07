package io.github.monun.kommand.plugin

import com.destroystokyo.paper.profile.PlayerProfile
import com.google.gson.JsonObject
import io.github.monun.kommand.Kommand
import io.github.monun.kommand.KommandArgument
import io.github.monun.kommand.StringType
import io.github.monun.kommand.getValue
import io.github.monun.kommand.wrapper.EntityAnchor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffectType

class KommandPlugin : JavaPlugin() {
    override fun onEnable() {
        val bool = KommandArgument.bool()
        val int = KommandArgument.int()
        val word = KommandArgument.string(StringType.SINGLE_WORD)
        val string = KommandArgument.string(StringType.QOUTABLE_PHRASE)
        val greedy = KommandArgument.string(StringType.GREEDY_PHRASE)

        Kommand.register("my") {
            then("age") {
                then("age" to int) {
                    executes {
                        val age: Int by it
                        Bukkit.broadcast(text("내 나이는 $age 살입니다."))
                    }
                }
            }
            then("flag") {
                then("flag" to bool) {
                    executes {
                        val flag: Boolean by it
                        Bukkit.broadcast(text("플래그 $flag"))
                    }
                }
            }
            then("word") {
                then("text" to word) {
                    executes {
                        val text: String by it
                        Bukkit.broadcast(text("word $text"))
                    }
                }
            }
            then("string") {
                then("text" to string) {
                    executes {
                        val text: String by it
                        Bukkit.broadcast(text("quote $text"))
                    }
                }
            }
            then("greedy") {
                then("text" to greedy) {
                    executes {
                        val text: String by it
                        Bukkit.broadcast(text("greedy $text"))
                    }
                }
            }
            then("color") {
                then("color" to KommandArgument.color()) {
                    executes {
                        val color: ChatColor by it
                        Bukkit.broadcast(text("$color color"))
                    }
                }
            }
            then("component") {
                then("component" to KommandArgument.component()) {
                    executes {
                        val component: Component by it
                        Bukkit.broadcast(component)
                    }
                }
            }
            then("compoundTag") {
                then("compoundTag" to KommandArgument.compoundTag()) {
                    executes {
                        val compoundTag: JsonObject by it
                        Bukkit.broadcast(text(compoundTag.toString()))
                    }
                }
            }
            then("dimension") {
                then("world" to KommandArgument.dimension()) {
                    executes {
                        val world: World by it
                        Bukkit.broadcast(text(world.toString()))
                    }
                }
            }
            then("entityAnchor") {
                then("entityAnchor" to KommandArgument.entityAnchor()) {
                    executes {
                        val entityAnchor: EntityAnchor by it
                        Bukkit.broadcast(text("anchor ${entityAnchor.name}"))
                    }
                }
            }
            then("entity") {
                then("entity" to KommandArgument.entity()) {
                    executes {
                        val entity: Entity by it
                        Bukkit.broadcast(text("entity $entity"))
                    }
                }
            }
            then("entities") {
                then("entities" to KommandArgument.entities()) {
                    executes {
                        val entities: Collection<Entity> by it
                        Bukkit.broadcast(text("entities $entities"))
                    }
                }
            }
            then("player") {
                then("player" to KommandArgument.player()) {
                    executes {
                        val player: Player by it
                        Bukkit.broadcast(text("player $player"))
                    }
                }
            }
            then("players") {
                then("players" to KommandArgument.players()) {
                    executes {
                        val players: Collection<Player> by it
                        Bukkit.broadcast(text("players $players"))
                    }
                }
            }
            then("summonable") {
                then("summonable" to KommandArgument.summonableEntity()) {
                    executes {
                        val summonable: NamespacedKey by it
                        Bukkit.broadcast(text("summonable $summonable"))
                    }
                }
            }
            then("profile") {
                then("profile" to KommandArgument.profile()) {
                    executes {
                        val profile: Collection<PlayerProfile> by it
                        Bukkit.broadcast(text(profile.toString()))
                    }
                }
            }
            then("enchantment") {
                then("enchantment" to KommandArgument.enchantment()) {
                    executes {
                        val enchantment: Enchantment by it
                        Bukkit.broadcast(text(enchantment.toString()))
                    }
                }
            }
            then("mobeffect") {
                then("mobeffect" to KommandArgument.mobEffect()) {
                    executes {
                        val mobeffect: PotionEffectType by it
                        Bukkit.broadcast(text("mobeffect ${mobeffect.name}"))
                    }
                }
            }
        }
    }
}