package com.app.nestedlist.data

sealed interface CommentItem {
    val id: Int
    val content: CharSequence
    val userId: Int
    val userName: CharSequence

    data class Loading(
        val page: Int = 0,
        val state: State = State.LOADING,
    ) : CommentItem {
        enum class State {
            IDLE, LOADING, LOADED_ALL
        }

        override val id: Int = 0
        override val content: CharSequence
            get() = when (state) {
                State.LOADED_ALL -> "加载更多"
                else -> "加载中..."
            }
        override val userId: Int = 0
        override val userName: CharSequence = ""
    }

    data class Level1(
        override val id: Int,
        override val content: CharSequence,
        override val userId: Int,
        override val userName: CharSequence,
        val level2Count: Int,
    ) : CommentItem

    data class Level2(
        override val id: Int,
        override val content: CharSequence,
        override val userId: Int,
        override val userName: CharSequence,
        val parentId: Int,
    ) : CommentItem

    data class Folding(
        val parentId: Int,
        val page: Int = 1,
        val pageSize: Int = 3,
        val state: State = State.IDLE
    ) : CommentItem {


        enum class State {
            IDLE, LOADING, LOADED_ALL
        }

        override val id: Int
            get() = parentId * 1000 + page
        override val content: CharSequence
            get() = if (state == State.LOADING) {
                "加载中"
            } else {
                when {
                    page <= 1 -> "展开20条回复"
                    else -> "展开更多"
                }
            }
        override val userId: Int = 0
        override val userName: CharSequence = ""
    }
}