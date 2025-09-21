package com.deliveranything.global.common;

import java.util.List;

public record SliceResponse<T>(
    List<T> content,
    String nextPageToken,
    boolean hasNext
) {

}
