package com.app.nestedlist.logic

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.app.nestedlist.R
import com.app.nestedlist.api.FakeApi
import com.app.nestedlist.data.CommentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StartExpandReducer(private val folding: CommentItem.Folding) : Reducer {
    override val reduce: suspend List<CommentItem>.() -> List<CommentItem>
        get() = {
            map {
                if (it is CommentItem.Folding && it == folding) {
                    it.copy(
                        state = CommentItem.Folding.State.LOADING
                    )
                } else {
                    it
                }
            }
        }

}

class ExpandReducer(private val folding: CommentItem.Folding) : Reducer {
    private val mapper by lazy { Entity2ItemMapper() }
    override val reduce: suspend List<CommentItem>.() -> List<CommentItem> = {
        val foldingIndex = indexOf(folding)
        val loaded = FakeApi.getLevel2Comments(folding.parentId, folding.page, folding.pageSize)
            .getOrNull()?.map(mapper::invoke) ?: emptyList()
        toMutableList().apply {
            addAll(foldingIndex, loaded)
        }.map {
            if (it is CommentItem.Folding && it == folding) {
                val state =
                    if (it.page > 5) CommentItem.Folding.State.LOADED_ALL else CommentItem.Folding.State.IDLE
                it.copy(page = it.page + 1, state = state)
            } else {
                it
            }
        }
    }

}

class FoldReducer(val folding: CommentItem.Folding) : Reducer {
    override val reduce: suspend List<CommentItem>.() -> List<CommentItem> = {
        delay(500)
        val parentIndex = indexOfFirst { it.id == folding.parentId }
        val foldingIndex = indexOf(folding)
        (this - subList(parentIndex + 3, foldingIndex).toSet()).map {
            if (it is CommentItem.Folding && it == folding) {
                it.copy(page = 1, state = CommentItem.Folding.State.IDLE)
            } else {
                it
            }
        }
    }

}

class StartLoadLv1Reducer : Reducer {
    override val reduce: suspend List<CommentItem>.() -> List<CommentItem>
        get() = {
            map {
                if (it is CommentItem.Loading) {
                    it.copy(state = CommentItem.Loading.State.LOADING)
                } else {
                    it
                }
            }
        }

}

class LoadLv1Reducer : Reducer {
    private val mapper by lazy { Entity2ItemMapper() }

    override val reduce: suspend List<CommentItem>.() -> List<CommentItem>
        get() = {
            val loading = get(size - 1) as CommentItem.Loading
            val loaded = FakeApi.getComment(loading.page + 1, 5).getOrNull()?.map(mapper::invoke)
                ?: emptyList()

            val grouped = loaded.groupBy {
                (it as? CommentItem.Level1)?.id ?: (it as? CommentItem.Level2)?.parentId
                ?: throw IllegalArgumentException("Invalid comment item")

            }.flatMap {
                it.value + CommentItem.Folding(parentId = it.key)
            }

            toMutableList().apply {
                removeAt(size - 1)
                this += grouped
                this += loading.copy(
                    state = CommentItem.Loading.State.IDLE,
                    page = loading.page + 1
                )
            }.toList()
        }

}


class ReplyReducer(private val commentItem: CommentItem, private val context: Context) : Reducer {


    override val reduce: suspend List<CommentItem>.() -> List<CommentItem> = {

        val mapper: Entity2ItemMapper by lazy { Entity2ItemMapper() }
        val content = withContext(Dispatchers.Main) {
            suspendCoroutine { continuation ->
                ReplyDialog(context) {
                    continuation.resume(it)
                }.show()
            }
        }
        val parentId = (commentItem as? CommentItem.Level1)?.id
            ?: (commentItem as? CommentItem.Level2)?.parentId ?: 0
        val replyItem = mapper.invoke(FakeApi.addComment(parentId, content))
        val insertIndex = indexOf(commentItem) + 1
        toMutableList().apply {
            add(insertIndex, replyItem)
        }
    }


}

class ReplyDialog(val context: Context, private val callback: (String) -> Unit) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_reply)
        val editText = findViewById<EditText>(R.id.content)
        findViewById<Button>(R.id.submit).setOnClickListener {
            if (editText.text.toString().isBlank()) {
                Toast.makeText(context, "评论不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            callback.invoke(editText.text.toString())
            dismiss()
        }
    }
}