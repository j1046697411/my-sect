package com.sect.game.domain.exception

sealed class SectException(
    message: String,
    userMessage: String,
) : DomainExceptionBase(message, userMessage) {
    class AtCapacityException(sectId: String, maxDisciples: Int) :
        SectException(
            message = "Sect $sectId is at capacity with max $maxDisciples disciples",
            userMessage = "宗门已达到最大弟子人数上限（${maxDisciples}人），无法继续招募",
        )

    class DiscipleNotFoundException(discipleId: String) :
        SectException(
            message = "Disciple $discipleId not found in sect",
            userMessage = "未找到该弟子：$discipleId",
        )

    class InsufficientResourcesException(
        requestedStones: Int,
        requestedHerbs: Int,
        requestedPills: Int,
        availableStones: Int,
        availableHerbs: Int,
        availablePills: Int,
    ) : SectException(
            message =
                "Insufficient resources: requested $requestedStones stones, " +
                    "$requestedHerbs herbs, $requestedPills pills but have " +
                    "$availableStones stones, $availableHerbs herbs, " +
                    "$availablePills pills",
            userMessage =
                "资源不足。当前拥有：灵石$availableStones、" +
                    "药材$availableHerbs、丹药$availablePills",
        )
}
