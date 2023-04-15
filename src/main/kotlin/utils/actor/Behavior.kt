package utils.actor

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.SendChannel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.actor.timer.SchedulerTimer


class ActorRef<in Message>(private val internalChannel: SendChannel<Message>, private val name: String) {
    suspend fun tell(message: Message) {
        try {
            internalChannel.send(message)
        }
        catch (e: Exception) {
            logger.error("cannot tell {} to {}", message!!::class.java.name, name, e)
        }
    }

    companion object {
        val logger: Logger = LogManager.getLogger()
    }
}

open class Behavior<in T> {

}

@OptIn(ObsoleteCoroutinesApi::class)
abstract class AbstractBehavior<T>(protected val scope: ActorScope<T>) : Behavior<T>() {
    protected val self = ActorRef(scope.channel, javaClass.name)

    abstract suspend fun onReceive(message: T): Behavior<T>

    protected suspend fun self(message: T) {
        scope.channel.send(message)
    }
}

class TimerBehavior<T>(val timerFunc: suspend (SchedulerTimer<T>) -> Behavior<T>) : Behavior<T>()

@OptIn(ObsoleteCoroutinesApi::class)
class SetupBehavior<T>(val factory: suspend (ActorScope<T>) -> Behavior<T>) : Behavior<T>()