package com.sect.game.data.mapper

import com.sect.game.data.dto.AttributesDto
import com.sect.game.data.dto.DiscipleDto
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm

object DiscipleMapper {
    fun toDto(disciple: Disciple): DiscipleDto {
        return DiscipleDto(
            id = disciple.id.value,
            name = disciple.name,
            realm = disciple.realm.order,
            attributes = AttributesMapper.toDto(disciple.attributes),
            cultivationProgress = disciple.cultivationProgress,
            fatigue = disciple.fatigue,
            health = disciple.health,
            lifespan = disciple.lifespan,
        )
    }

    fun toDomain(dto: DiscipleDto): Disciple {
        return Disciple(
            id = DiscipleId(dto.id),
            name = dto.name,
            realm = Realm.fromOrder(dto.realm) ?: Realm.LianQi,
            attributes = AttributesMapper.toDomain(dto.attributes),
            cultivationProgress = dto.cultivationProgress,
            fatigue = dto.fatigue,
            health = dto.health,
            lifespan = dto.lifespan,
        )
    }
}

object AttributesMapper {
    fun toDto(attributes: Attributes): AttributesDto {
        return AttributesDto(
            spiritRoot = attributes.spiritRoot,
            talent = attributes.talent,
            luck = attributes.luck,
        )
    }

    fun toDomain(dto: AttributesDto): Attributes {
        return Attributes(
            spiritRoot = dto.spiritRoot,
            talent = dto.talent,
            luck = dto.luck,
        )
    }
}
