package vendingmachine

import camp.nextstep.edu.missionutils.Console
import camp.nextstep.edu.missionutils.Randoms
import kotlin.jvm.JvmStatic

object Application {

    private val coinSet:MutableMap<Int, Int> = mutableMapOf(500 to 0, 100 to 0, 50 to 0, 10 to 0)

    private val itemSet:MutableMap<String, Array<Int>> = mutableMapOf()

    private val remainSet:MutableMap<Int, Int> = mutableMapOf(500 to 0, 100 to 0, 50 to 0, 10 to 0)

    private var minPrice:Int = 0
    private var itemTotalCount:Int = 0
    private var totalRemain:Int = 0

    @JvmStatic
    fun main(args: Array<String>) {
        // TODO: 프로그램 구현

        //초기 값
        var totalCoin = initMachine()

        setCoins(totalCoin, listOf(500,100,50,10))
        printSet(SetName.COIN)

        println()
        setItems()
        printSet(SetName.ITEM)

        println()
        insertMoney()


        printSet(SetName.REMAIN)
    }

    private fun initMachine():Int{
        println("자판기가 보유하고 있는 금액 입력")
        var read:Int? = Console.readLine().toIntOrNull()
        if(read == null || read % 10 != 0){
            printError("금액 오류")
            return initMachine()
        }
        return read
    }

    private fun setCoins(totalCoin:Int, tempCoinList:List<Int>){
        val picked = tempCoinList[0]
        val maxPossible = totalCoin / picked

        if(picked == 10){
            coinSet[picked] = maxPossible
            return
        }
        var coinCount = Randoms.pickNumberInList((0..maxPossible).toList())
        coinSet[picked] = coinCount
        if(tempCoinList.minus(picked).isNotEmpty()) {
            setCoins(totalCoin-(picked * coinCount), tempCoinList.minus(picked))
        }
    }

    private fun setItems(){
        println("상품 입력")
        var read:String = Console.readLine()
        if(read.last() != ';') read += ";"
        println(read)
        val regex = Regex("\\[[ㄱ-ㅎ|ㅏ-ㅣ|가-힣|0-9]+,[0-9]+,[0-9]+\\];+")
        if(!regex.matches(read)){
            printError("상품 오류")
            return setItems()
        }
        val itemStrArray = read.split(';')
        for(itemStr in itemStrArray){
            if(itemStr.isEmpty()) return
            val itemArray = itemStr.replace("[","").replace("]","").split(',')
            //상품 가격은 100원부터 시작하며, 10원으로 나누어떨어져야 한다.
            val name = itemArray[0]
            val price = itemArray[1].toIntOrNull()
            val count = itemArray[2].toIntOrNull()
            if(price != null && count != null && (price < 100 || price % 10 > 0)) {
                printError("상품 오류")
                return setItems()
            }
            itemSet[name] = arrayOf(price!!, count!!)
            if(minPrice == 0 || price < minPrice ){
                minPrice = price
            }
            itemTotalCount += count
        }
    }
    private fun insertMoney(){
        println("투입 금액을 입력해주세요.")
        var insertedMoney:Int? = Console.readLine().toIntOrNull()
        if(insertedMoney == null || insertedMoney % 10 > 0){
            printError("투입금액 오류")
            return insertMoney()
        }

        buyItem(insertedMoney)
    }

    private fun buyItem(insertedMoney: Int) {
        totalRemain = insertedMoney
        println()
        println("투입금액: $insertedMoney")
        //남은 금액이 상품의 최저 가격보다 적거나, 모든 상품이 소진된 경우 바로 잔돈을 돌려준다.
        if(insertedMoney < minPrice || itemTotalCount <= 0){
            //잔돈 반환
            return returnRemain()
        }

        println("구매할 상품명을 입력해 주세요.")
        var read = Console.readLine()
        if(itemSet[read]?.get(0) == null || itemSet[read]?.get(1) == 0){
            printError("구매 상품 없음")
            printSet(SetName.ITEM)
            return buyItem(insertedMoney)
        }
        val price = itemSet[read]?.get(0)!!
        var count = itemSet[read]?.get(1)!!
        val remain = insertedMoney - itemSet[read]?.get(0)!!
        //println(remain)

        itemSet[read] = arrayOf(price, count-1)
        itemTotalCount -= 1


        //printSet(SetName.ITEM)
        buyItem(remain)
    }

    private fun returnRemain(){
        println("잔돈")
        //loop
        for((k, v) in coinSet) { //3000    500 3  3510
            calculate(k, v)
        }
    }

    private fun calculate(coinValue:Int, count:Int){
        if(totalRemain >= coinValue && count > 0){
            coinSet[coinValue] = count-1
            var temp: Int? = remainSet[coinValue]
            if(temp != null){
                remainSet[coinValue] = temp + 1
            }
            totalRemain -= coinValue
            calculate(coinValue, count-1)
        }
    }

    private fun printSet(setName: SetName){
        if(setName.toString() == "COIN"){
            for((k, v) in coinSet){
                println("${k}원 - ${v}개")
            }
        }
        if(setName.toString() == "REMAIN"){
            for((k,v) in remainSet){
                println("${k}원 ${v}개")
            }
        }
        if(setName.toString() == "ITEM"){
            for((k, v) in itemSet){
                println("$k - ${v[0]}원 ${v[1]}개")
            }
        }
    }
    private fun printError(message:String){
        println("[ERROR] $message")
    }
}