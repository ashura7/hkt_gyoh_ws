package utils.actor

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ActorScope
import utils.actor.Behaviors.spawnChild


@OptIn(ObsoleteCoroutinesApi::class)
class SpawnProtocol(scope: ActorScope<Command>) : AbstractBehavior<SpawnProtocol.Command>(scope) {
    sealed interface Command

    override suspend fun onReceive(message: Command): Behavior<Command> {
        return this
    }

    fun <T> spawn(name: String, factory: () -> Behavior<T>): ActorRef<T> {
        return scope.spawnChild(name = name, factory = factory)
    }
}