package org.example.project.snake.model

/**
 * 表示贪吃蛇游戏中的蛇
 * 
 * @param body 蛇身体的位置列表，第一个元素是蛇头
 * @param direction 当前移动方向
 */
data class Snake(
    val body: List<Position>,
    val direction: Direction
) {
    
    /**
     * 蛇头的位置
     */
    val head: Position
        get() = body.first()
    
    /**
     * 蛇身的长度
     */
    val length: Int
        get() = body.size
    
    /**
     * 移动蛇到下一个位置
     * 
     * @param newDirection 新的移动方向（可选，如果不提供则使用当前方向）
     * @param grow 是否增长（吃到食物时为true）
     * @return 移动后的新蛇对象
     */
    fun move(newDirection: Direction? = null, grow: Boolean = false): Snake {
        // 确定实际的移动方向
        val actualDirection = newDirection?.let { newDir ->
            // 防止蛇直接反向移动（撞到自己）
            if (body.size > 1 && newDir == direction.opposite()) {
                direction  // 保持原方向
            } else {
                newDir     // 使用新方向
            }
        } ?: direction
        
        // 计算新的蛇头位置
        val newHead = head.move(actualDirection)
        
        // 构建新的身体列表
        val newBody = if (grow) {
            // 如果需要增长，保留所有身体段落
            listOf(newHead) + body
        } else {
            // 正常移动，去掉尾巴
            listOf(newHead) + body.dropLast(1)
        }
        
        return copy(
            body = newBody,
            direction = actualDirection
        )
    }
    
    /**
     * 检查蛇是否撞到自己
     * 
     * @return 如果撞到自己返回true，否则返回false
     */
    fun checkSelfCollision(): Boolean {
        // 检查蛇头是否与身体的任何部分重叠
        return body.drop(1).contains(head)
    }
    
    /**
     * 检查蛇是否撞到墙壁
     * 
     * @param gameWidth 游戏区域宽度
     * @param gameHeight 游戏区域高度
     * @return 如果撞到墙壁返回true，否则返回false
     */
    fun checkWallCollision(gameWidth: Int, gameHeight: Int): Boolean {
        return !head.isInBounds(gameWidth, gameHeight)
    }
    
    /**
     * 检查蛇是否吃到食物
     * 
     * @param food 食物位置
     * @return 如果吃到食物返回true，否则返回false
     */
    fun hasEatenFood(food: Position): Boolean {
        return head == food
    }
    
    /**
     * 获取蛇占据的所有位置
     * 
     * @return 蛇身体占据的位置集合
     */
    fun getOccupiedPositions(): Set<Position> {
        return body.toSet()
    }
    
    companion object {
        /**
         * 创建初始状态的蛇
         * 
         * @param startPosition 起始位置（蛇头位置）
         * @param initialDirection 初始移动方向
         * @param initialLength 初始长度（默认为3）
         * @return 初始状态的蛇对象
         */
        fun createInitial(
            startPosition: Position,
            initialDirection: Direction = Direction.RIGHT,
            initialLength: Int = 3
        ): Snake {
            // 根据初始方向创建蛇身
            val body = mutableListOf<Position>()
            var currentPos = startPosition
            
            // 添加蛇头
            body.add(currentPos)
            
            // 添加身体段落（向相反方向延伸）
            val oppositeDirection = initialDirection.opposite()
            repeat(initialLength - 1) {
                currentPos = currentPos.move(oppositeDirection)
                body.add(currentPos)
            }
            
            return Snake(
                body = body,
                direction = initialDirection
            )
        }
    }
}
