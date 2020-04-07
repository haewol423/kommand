package com.github.noonmaru.kommand

import com.google.common.collect.ImmutableMap
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class KommandDispatcher(children: Map<PluginCommand, LiteralKommandBuilder>) {
    companion object {
        private const val unknownCommandErrorMessage = "알 수 없는 명령입니다."
    }

    private val children: Map<PluginCommand, LiteralKommand>

    init {
        val builds = HashMap<PluginCommand, LiteralKommand>()

        for ((command, builder) in children) {
            if (builder.executor == null) {
                builder.executes {
                    val sender = it.sender
                    sender.sendMessage(getUsage(sender, it.command).toTypedArray())
                }
            }

            builds[command] = builder.build()
        }

        this.children = ImmutableMap.copyOf(builds)

        val adapter = KommandAdapter(this)

        for (command in children.keys) {
            command.setExecutor(adapter)
            command.tabCompleter = adapter
        }
    }

    private fun parse(sender: CommandSender, command: Command, args: Array<out String>): KommandContext {
        return children[command]?.let { root ->
            root.requirement?.let { requirement ->
                if (!sender.requirement()) {
                    throw KommandSyntaxException(unknownCommandErrorMessage)
                }
            }

            val nodes = ArrayList<Kommand>()
            nodes.add(root)
            var last: Kommand = root

            for (i in 0 until args.count()) {
                if (last.children.isEmpty()) throw KommandSyntaxException("인수를 끝내는 공백이 필요하지만, 후행 데이터가 입력되었습니다")

                val arg = args[i]
                val child = last.getChild(arg) ?: throw KommandSyntaxException(unknownCommandErrorMessage)

                child.requirement?.let { requirement ->
                    if (!sender.requirement()) {
                        throw KommandSyntaxException(unknownCommandErrorMessage)
                    }
                }

                nodes += child
                last = child
            }

            val executor = last.executor ?: throw KommandSyntaxException(unknownCommandErrorMessage)

            KommandContext(command, args, nodes).apply {
                this.executor = executor
            }
        } ?: throw KommandSyntaxException(unknownCommandErrorMessage)
    }

    internal fun dispatch(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ) {
        runCatching {
            val context = parse(sender, command, args).apply {
                this.sender = sender
                this.alias = alias
            }
            context.executor.invoke(context)
        }.onFailure { throwable ->
            if (throwable is KommandSyntaxException) {
                //Syntax 에러
                sender.sendFeedback("${ChatColor.RED}${throwable.syntaxMessage}")
            } else {
                //예외 메시지 출력
                throwable.printStackTrace()

                if (sender is Player) {
                    sender.sendMessage("${ChatColor.YELLOW}${throwable.javaClass.name}: ${throwable.message}")
                    for (stackTraceElement in throwable.stackTrace) {
                        sender.sendMessage("${ChatColor.YELLOW}  at $stackTraceElement")
                    }
                }
            }
        }
    }

    internal fun computeSuggestion(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return this.children[command]?.let { root ->
            val nodes = ArrayList<Kommand>()
            nodes.add(root)
            var last: Kommand = root

            for (i in 0 until args.count() - 1) {
                if (last.children.isEmpty())
                    return emptyList()

                val arg = args[i]
                val child = last.getChild(arg) ?: return emptyList()

                nodes += child
                last = child
            }

            val context = KommandContext(command, args, nodes).apply {
                this.sender = sender
                this.alias = alias
            }
            val target = args.last()
            val list = ArrayList<String>()

            for (child in last.children) {
                if (child is ArgumentKommand)
                    list += child.argument.listSuggestion(context, target)
                else {
                    val name = child.name

                    if (name.startsWith(target, true))
                        list += name
                }
            }

            list
        } ?: emptyList()
    }

    private fun getUsage(sender: CommandSender, command: Command): List<String> {
        return children[command]!!.let { kommand ->
            kommand.usages(sender)
        }
    }
}

fun LiteralKommand.usages(sender: CommandSender): List<String> {
    val list = ArrayList<String>()
    val prefix = "/$name"

    for (child in children) {
        val requirement = child.requirement

        if (requirement == null || sender.requirement())
            child.computeUsages(sender, this, list, StringBuilder(prefix))
    }

    return list
}

fun Kommand.computeUsages(sender: CommandSender, parent: Kommand, list: MutableList<String>, builder: StringBuilder) {
    builder.append(' ')
    if (this is ArgumentKommand) {
        val brace = if (executor != null && parent.executor != null && children.isEmpty()) "[]" else "<>"
        builder.append(brace[0]).append(name).append(brace[1])
    } else {
        builder.append(name)
    }

    for (child in children) {
        val requirement = child.requirement

        if (requirement == null || sender.requirement())
            child.computeUsages(sender, this, list, StringBuilder(builder))
    }

    if (executor != null && children.isEmpty()) {
        list += builder.toString()
    }
}

//인수명령어
//현재 명령에 executor 존재
//상위 명령에 executor가 존재
//하위 명령어 없음

class KommandDispatcherBuilder(
    private val plugin: JavaPlugin
) {
    private val buildersByName = LinkedHashMap<PluginCommand, LiteralKommandBuilder>()

    fun register(name: String, init: KommandBuilder.() -> Unit) {
        val command = plugin.getCommand(name) ?: throw IllegalArgumentException("[$name] is unknown command")

        buildersByName[command] = LiteralKommandBuilder(name).apply(init)
    }

    fun build(): KommandDispatcher {
        return KommandDispatcher(buildersByName)
    }
}

internal class KommandAdapter(private val dispatcher: KommandDispatcher) : TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): Boolean {
        dispatcher.dispatch(sender, command, alias, args)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return dispatcher.computeSuggestion(sender, command, alias, args)
    }
}