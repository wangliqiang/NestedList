package com.app.nestedlist.logic

import com.app.nestedlist.data.CommentItem

interface Reducer {
    val reduce: suspend List<CommentItem>.() -> List<CommentItem>
}