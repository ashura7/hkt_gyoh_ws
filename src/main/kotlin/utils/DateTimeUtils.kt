package utils

import java.time.LocalDate
import java.time.LocalDateTime

val ROOT_DATETIME: LocalDateTime = LocalDateTime.parse("2022-12-31T23:59")

val ROOT_DATE: LocalDate = ROOT_DATETIME.toLocalDate()