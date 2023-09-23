package com.app.nestedlist.api

import com.app.nestedlist.data.CommentLevel1
import com.app.nestedlist.data.CommentLevel2
import com.app.nestedlist.data.ICommentEntity
import kotlinx.coroutines.delay

object FakeApi {

    private var id = 0


    suspend fun getComment(page: Int, pageSize: Int = 5): Result<List<ICommentEntity>> {
        delay(500)
        val list = (0 until pageSize).map {
            val id = this.id++
            CommentLevel1(
                id = id,
                content = "一级评论${id}",
                userId = 1,
                userName = "一级评论员",
                level2Count = 20
            )
        }.map {
            listOf(
                it,
                CommentLevel2(
                    id = id++,
                    content = "二级评论${id}",
                    userId = 2,
                    userName = "二级评论员",
                    parentId = it.id,
                    hot = true,
                ),
                CommentLevel2(
                    id = id++,
                    content = "我是二级评论${id}",
                    userId = 2,
                    userName = "二级评论员",
                    parentId = it.id,
                    hot = true,
                )
            )
        }.flatten()
        return Result.success(list)
    }

    suspend fun getLevel2Comments(
        parentId: Int,
        page: Int,
        pageSize: Int = 3
    ): Result<List<ICommentEntity>> {
        delay(500)
        if (page > 5) return Result.success(emptyList())
        val list = (0 until pageSize).map {
            CommentLevel2(
                id = id++,
                content = "二级评论${id}",
                userId = 2,
                userName = "二级评论员",
                parentId = parentId
            )
        }
        return Result.success(list)
    }

    suspend fun addComment(id: Int, content: String): CommentLevel2 {
        delay(400)
        this.id++
        return CommentLevel2(
            id = this.id++,
            content,
            userId = 3,
            userName = "测试",
            parentId = id
        )

    }

}