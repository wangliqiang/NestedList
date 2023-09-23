package com.app.nestedlist.logic

import com.app.nestedlist.data.CommentItem
import com.app.nestedlist.data.CommentLevel1
import com.app.nestedlist.data.CommentLevel2
import com.app.nestedlist.data.ICommentEntity
import com.app.nestedlist.ext.makeHot

class Entity2ItemMapper : Mapper<ICommentEntity, CommentItem> {
    override fun invoke(entity: ICommentEntity): CommentItem {
        return when (entity) {
            is CommentLevel1 -> {
                CommentItem.Level1(
                    id = entity.id,
                    content = entity.content,
                    userId = entity.userId,
                    userName = entity.userName,
                    level2Count = entity.level2Count,
                )
            }

            is CommentLevel2 -> {
                CommentItem.Level2(
                    id = entity.id,
                    content = if (entity.hot) entity.content.makeHot() else entity.content,
                    userId = entity.userId,
                    userName = entity.userName,
                    parentId = entity.parentId,
                )
            }

            else -> {
                throw IllegalArgumentException("not implemented entity: $entity")
            }
        }
    }
}