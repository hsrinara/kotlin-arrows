import arrow.core.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import arrow.typeclasses.*
import arrow.instances.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

fun parse(s: String): Either<NumberFormatException, Int> =
        if (s.matches(Regex("-?[0-9]+"))) Either.Right(s.toInt())
        else Either.Left(NumberFormatException("$s is not a valid integer."))

fun reciprocal(i: Int): Either<IllegalArgumentException, Double> =
        if (i == 0) Either.Left(IllegalArgumentException("Cannot take reciprocal of 0."))
        else Either.Right(1.0 / i)

fun stringify(d: Double): String = d.toString()

fun magic(s: String): Either<Exception, String> =
        parse(s).flatMap { reciprocal(it) }.map { stringify(it) }

sealed class LivingThing

data class Person(val name: String, val age: Int) : LivingThing()

data class Dog(val name: String) : LivingThing()

fun printsNameOfLivingThing(livingThing: LivingThing) {
    when (livingThing) {
        is Person -> println(" Person " + livingThing.name)
        is Dog -> println(" Dog " + livingThing.name)
    }
}

fun multiply(a: Int, b: Int): Int = a * b

fun printLivingThingTwice(livingThing: LivingThing) {

    fun printThing(a: LivingThing): Unit = println(a)

    printThing(livingThing)
    printThing(livingThing)
}

suspend fun <T> CompletableFuture<T>.await(): T =
        suspendCoroutine { cont: Continuation<T> ->
            whenComplete { result, exception ->
                if (exception == null) // the future has been completed normally
                    cont.resume(result)
                else // the future has completed with an exception
                    cont.resumeWithException(exception)
            }
        }


private fun createPersonAndDog(): Pair<Person, Dog> {
    println("3 * 4 = " + multiply(3, 4))
    println(magic("34"))
    println(magic("0"))
    println(magic("saf"))
    println("hello world")
    val person = Person("Bob", 23)
    val dog = Dog("doggy")
    println(person)
    println(dog)
    return Pair(person, dog)
}


private fun myAsyncAwaitTest() {
    fun doSimpleBlockingTest() {
        launch {
            delay(1000)
            println("Hello")
        }

        Thread.sleep(2000) // wait for 2 seconds
        println("Stop")
    }

    fun doSimpleAsyncAwaitTest() {
        val deferred = (1..1_000_000).map { n ->
            async {
                n
            }
        }

        runBlocking {
            val sum = deferred.sumBy { it.await() }
            println("Sum: $sum")
        }
    }

    fun doACompleteableFutureStyleThing() {

        fun lookupDogFromDatabase(id: String): CompletableFuture<Dog> =
                CompletableFuture.supplyAsync { Dog("harry") }



        fun doSomethingWithFutureDog(futureDog: CompletableFuture<Dog>): Unit = {

        }

        val fut = future {
            val futureDog = lookupDogFromDatabase("34")
            val futureDog2 = lookupDogFromDatabase("32344")
            doSomethingWithFutureDog(futureDog)
            val realDog = futureDog.await()
            val realDog2 = futureDog2.await()

            println("completafutredog = ${realDog.name}")
            println("completafutredog = ${realDog2.name}")
        }

        fut.join()
    }

//    doSimpleBlockingTest()
//    doSimpleAsyncAwaitTest()
    doACompleteableFutureStyleThing()
}


data class Country(val code: Option<String>)
data class Address(val id: Int, val country: Option<Country>)
data class HumanPerson(val name: String, val address: Option<Address>)

data class Country2(val code: String?)
data class Address2(val id: Int, val country: Country2?)
data class HumanPerson2(val name: String, val address: Address2?)


data class Country3(val code: Optional<String>)
data class Address3(val id: Int, val country: Optional<Country3>)
data class HumanPerson3(val name: String, val address: Optional<Address3>)

private fun myPatternMatchingTest(dog: Dog, person: Person) {
    printsNameOfLivingThing(dog)
    printsNameOfLivingThing(person)
    printLivingThingTwice(person)
}

fun getCountryCodeFpStyle(maybePerson: Option<HumanPerson>): Option<String> =
        ForOption extensions {
            binding {
                val humanPerson = maybePerson.bind()
                val address = humanPerson.address.bind()
                val country = address.country.bind()
                val code = country.code.bind()
                code
            }.fix()
        }

private fun getCountryCodeJavaStyle(): Optional<String> {
    val javaStyle = java.util.Optional.of(HumanPerson3("alice",
            Optional.of(Address3(3, Optional.of(Country3(Optional.of("UK")))))))


    if (javaStyle.isPresent) {
        val person3 = javaStyle.get()
        if (person3.address.isPresent) {
            val address3 = person3.address.get()
            if (address3.country.isPresent) {
                return address3.country.get().code
            }
        }
    }
    return Optional.empty()
}

fun myOptionTest() {
    println(getCountryCodeFpStyle(Some(HumanPerson("bobby", Some(Address(3, Some(Country(Some("UK")))))))))

    println(getCountryCodeJavaStyle())
    println(getCountryCodeKotlinInteropStyle())
    println(getCountryCodePureKotlinStyle())
}

fun getCountryCodeKotlinInteropStyle(): Optional<String> {
    val javaStyle = java.util.Optional.of(HumanPerson3("alice",
            Optional.of(Address3(3, Optional.of(Country3(Optional.of("UK")))))))
    return javaStyle.flatMap {
        it.address.flatMap {
            it.country.flatMap {
                it.code
            }
        }
    }
}

fun getCountryCodePureKotlinStyle(): String? {
    val humanPerson2: HumanPerson2? = HumanPerson2("bobby", Address2(3, Country2("UK")))
    return humanPerson2?.address?.country?.code
}

fun main(args: Array<String>) {
//    val (person, dog) = createPersonAndDog()
//    myPatternMatchingTest(dog, person)
    myAsyncAwaitTest()
//    myOptionTest()

}
