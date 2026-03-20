package com.sect.game.data.mapper

import com.sect.game.data.dto.ResourcesDto
import com.sect.game.data.dto.SectDto
import com.sect.game.domain.entity.Resources
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.SectId

object SectMapper {
    fun toDto(sect: Sect): SectDto {
        return SectDto(
            id = sect.id.value,
            name = sect.name,
            disciples = sect.disciples.values.map { DiscipleMapper.toDto(it) },
            resources = ResourcesMapper.toDto(sect.resources),
            maxDisciples = sect.maxDisciples
        )
    }

    fun toDomain(dto: SectDto): Sect {
        val disciples = dto.disciples.associateBy(
            keySelector = { DiscipleId(it.id) },
            valueTransform = { DiscipleMapper.toDomain(it) }
        )
        return Sect(
            id = SectId(dto.id),
            name = dto.name,
            resources = ResourcesMapper.toDomain(dto.resources),
            maxDisciples = dto.maxDisciples
        ).let { sect ->
            disciples.forEach { (id, disciple) ->
                sect.addDisciple(disciple)
            }
            sect
        }
    }
}

object ResourcesMapper {
    fun toDto(resources: Resources): ResourcesDto {
        return ResourcesDto(
            spiritStones = resources.spiritStones,
            herbs = resources.herbs,
            pills = resources.pills
        )
    }

    fun toDomain(dto: ResourcesDto): Resources {
        return Resources(
            spiritStones = dto.spiritStones,
            herbs = dto.herbs,
            pills = dto.pills
        )
    }
}
