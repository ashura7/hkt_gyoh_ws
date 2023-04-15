package utils.actor

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.actor.timer.DelayedMessage
import utils.actor.timer.SingleTimerData
import utils.actor.timer.SchedulerTimer
import utils.actor.*

object Behaviors {
    val logger: Logger = LogManager.getLogger()

    // dummy object
    private val o1 = Behavior<Any>()

    fun <T> same(): Behavior<T> {
        return o1 as Behavior<T>
    }

    private val o2 = Behavior<Any>()

    fun <T> stopped(): Behavior<T> {
        return o2 as Behavior<T>
    }


    fun <T> withTimer(doWithTimer: suspend (SchedulerTimer<T>) -> Behavior<T>): Behavior<T> {
        return TimerBehavior(doWithTimer)
    }


    @OptIn(ObsoleteCoroutinesApi::class)
    fun <T> setup(factory: suspend (ActorScope<T>) -> Behavior<T>): Behavior<T> {
        return SetupBehavior(factory)
    }


    // theo mặc định khi actor bị crash nó sẽ stop(cancel) scope đã tạo ra actor
    // khi actor stop an toàn thì sẽ không affect đến scope ban đầu
    // nếu createNewScope = true thì sẽ create một scope mới cho actor kèm với supervisor
    // và actor crash sẽ không gây stop scope ban đầu
    @OptIn(ObsoleteCoroutinesApi::class)
    private fun <T> CoroutineScope.spawn(
        debug: Boolean = false,
        createNewScope: Boolean = false,
        name: String,
        factory: suspend () -> Behavior<T>
    ): ActorRef<T> {
        val scopeSpawnActor =
            if (createNewScope) {
                val newContext = coroutineContext + SupervisorJob()
                CoroutineScope(newContext)
            } else {
                this
            }
        val internalChannel = scopeSpawnActor.actor(capacity = 10000) {

            Dispatchers.Unconfined

            val timerMan = SchedulerTimer<T>(this, debug)
            var state = factory()


            suspend fun unwrapBehavior(behavior: Behavior<T>): Behavior<T> {
                var tmp: Behavior<T> = behavior
                while (tmp is TimerBehavior<T> || tmp is SetupBehavior<T>) {
                    if (tmp is TimerBehavior<T>) {
                        tmp = tmp.timerFunc(timerMan)
                    } else if (tmp is SetupBehavior<T>) {
                        tmp = tmp.factory(this as ActorScope<T>)
                    }
                }
                return tmp
            }
            if (state !is AbstractBehavior<T>) {
                state = unwrapBehavior(state)
            }

            if (state !is AbstractBehavior<T>) {
                throw IllegalArgumentException("init state must be abstract behavior")
            }

            channel.consumeEach {

                var messageToProcess: T? = null
                if (it is DelayedMessage<*>) {
                    // un-check cast
                    val dMsg = it as DelayedMessage<T>
                    val (msg, key, genFromMessage) = dMsg
                    if (!timerMan.keyExist(key)) {
                        // timer này đã bị huỷ, không xử lý message này
                        if (debug) {
                            logger.debug("msg {} come but key not exist, ignore", msg)
                        }

                        return@consumeEach
                    }
                    val currentGenThisKey = timerMan.getCurrentGeneration(key)
                    // timer này bị huỷ khi message đã được gửi
                    // không xử lý
                    if (currentGenThisKey != genFromMessage) {
                        if (debug) {
                            logger.debug("msg {} come but generation outdated, ignore", msg)
                        }
                        return@consumeEach
                    }

                    // ở đây chắc chắc state là Abstract behavior


                    // check nếu là single timer thì xoá
                    // vì nó đã hoàn thành nhiệm vụ
                    val timerData = timerMan.getTimerData(key)
                    if (timerData is SingleTimerData) {
                        timerMan.removeKey(key, false)
                    }
                    messageToProcess = msg
                } else {
                    // message bình thường
                    messageToProcess = it as T
                }


                if (debug) {
                    logger.debug("msg internal come {}", messageToProcess)
                }


                // impossible
                // but a bug here so we will fix this
                //if (state == same<T>()) return@consumeEach


                var tmp: Behavior<T> = (state as AbstractBehavior<T>).onReceive(messageToProcess)
                if (tmp == same<T>()) return@consumeEach


                // kill actor
                // và kill tất cả timer, etc...
                // we are safe here
                if (tmp == stopped<T>()) {
                    if (debug) {
                        logger.debug("stopped come, cancel the channel, scope is {}", this)
                    }
                    this.cancel()
                    return@consumeEach
                }

                if (tmp !is AbstractBehavior<T>) {
                    tmp = unwrapBehavior(tmp)
                }

                // recheck with same and stopped here???
                // sau chỗ này state bắt buộc phải là AbstractBehavior
                if (tmp == stopped<T>()) {
                    if (debug) {
                        logger.debug("cancel channel after unwrap timer behavior")
                    }
                    //timerMan.cancelAll()
                    this.cancel()
                    return@consumeEach
                }
                if (tmp is AbstractBehavior<T>) {
                    state = tmp
                } else if (tmp == same<T>()) {
                    return@consumeEach
                } else {
                    logger.error("logic had some problem, review code")
                }
            }
        }

        return ActorRef(internalChannel as SendChannel<T>, name)
    }

    // actor mới này nếu stop an toàn thì không affect đến parent
    // nếu crash sẽ gây crash parents
    // parents stop sẽ stop tất cả các con
    @OptIn(ObsoleteCoroutinesApi::class)
    fun <T> ActorScope<*>.spawnChild(
        debug: Boolean = false,
        name: String,
        factory: suspend () -> Behavior<T>
    ): ActorRef<T> {
        return spawn(debug, createNewScope = false, name, factory)
    }

    fun <T> CoroutineScope.spawnBehavior(
        debug: Boolean = false,
        name: String,
        factory: suspend () -> Behavior<T>
    ): ActorRef<T> {
        return spawn(debug, createNewScope = true, name, factory)
    }


}