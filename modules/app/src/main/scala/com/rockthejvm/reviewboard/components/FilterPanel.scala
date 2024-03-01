package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import org.scalajs.dom
import com.rockthejvm.reviewboard.domain.data.CompanyFilter
import com.rockthejvm.reviewboard.core.ZJS.*

/** A filter panel for the companies page.
  */
object FilterPanel {

  val GROUP_LOCATIONS  = "Locations"
  val GROUP_COUNTRIES  = "Countries"
  val GROUP_INDUSTRIES = "Industries"
  val GROUP_TAGS       = "Tags"
  case class CheckValueEvent(groupName: String, value: String, checked: Boolean)

  val checkEvents = EventBus[CheckValueEvent]()

  val possibleFilter = EventBus[CompanyFilter]()
//  val possibleFilter = Var[CompanyFilter](CompanyFilter.empty)

  val state: Signal[CompanyFilter] =
    checkEvents.events.scanLeft(
      Map.empty[String, Set[String]].withDefaultValue(Set.empty[String])
    ) {
      case (currentMap, CheckValueEvent(groupName, value, checked)) =>
        println(groupName)
        if checked then currentMap + (groupName -> (currentMap(groupName) + value))
        else currentMap + (groupName            -> (currentMap(groupName) - value))
    }.map(checkMap =>
      CompanyFilter(
        locations = checkMap(GROUP_LOCATIONS).toList,
        countries = checkMap(GROUP_COUNTRIES).toList,
        industries = checkMap(GROUP_INDUSTRIES).toList,
        tags = checkMap(GROUP_TAGS).toList
      )
    )

  def apply() = div(
    onMountCallback(_ =>
//      useBackend(_.company.allFiltersEndpoint(())).map(possibleFilter.set).runJs
      useBackend(_.company.allFiltersEndpoint(())).emitTo(possibleFilter)
    ),
    child.text <-- state.map(_.toString()),
    cls    := "accordion accordion-flush",
    idAttr := "accordionFlushExample",
    div(
      cls := "accordion-item",
      h2(
        cls    := "accordion-header",
        idAttr := "flush-headingOne",
        button(
          cls                                         := "accordion-button",
          idAttr                                      := "accordion-search-filter",
          `type`                                      := "button",
          htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
          htmlAttr("data-bs-target", StringAsIsCodec) := "#flush-collapseOne",
          htmlAttr("aria-expanded", StringAsIsCodec)  := "true",
          htmlAttr("aria-controls", StringAsIsCodec)  := "flush-collapseOne",
          div(
            cls := "jvm-recent-companies-accordion-body-heading",
            h3(
              span("Search"),
              " Filters"
            )
          )
        )
      ),
      div(
        cls                                          := "accordion-collapse collapse show",
        idAttr                                       := "flush-collapseOne",
        htmlAttr("aria-labelledby", StringAsIsCodec) := "flush-headingOne",
        htmlAttr("data-bs-parent", StringAsIsCodec)  := "#accordionFlushExample",
        div(
          cls := "accordion-body p-0",
          renderFilterOptions(GROUP_LOCATIONS, _.locations),
          renderFilterOptions(GROUP_COUNTRIES, _.countries),
          renderFilterOptions(GROUP_INDUSTRIES, _.industries),
          renderFilterOptions(GROUP_TAGS, _.tags),
          div(
            cls := "jvm-accordion-search-btn",
            button(
              cls    := "btn btn-primary",
              `type` := "button",
              "Apply Filters"
            )
          )
        )
      )
    )
  )

  def renderFilterOptions(groupName: String, optionsFun: CompanyFilter => List[String]) =
    div(
      cls := "accordion-item",
      h2(
        cls    := "accordion-header",
        idAttr := s"heading$groupName",
        button(
          cls                                         := "accordion-button collapsed",
          `type`                                      := "button",
          htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
          htmlAttr("data-bs-target", StringAsIsCodec) := s"#collapse$groupName",
          htmlAttr("aria-expanded", StringAsIsCodec)  := "false",
          htmlAttr("aria-controls", StringAsIsCodec)  := s"collapse$groupName",
          groupName
        )
      ),
      div(
        cls                                          := "accordion-collapse collapse",
        idAttr                                       := s"collapse$groupName",
        htmlAttr("aria-labelledby", StringAsIsCodec) := "headingOne",
        htmlAttr("data-bs-parent", StringAsIsCodec)  := "#accordionExample",
        div(
          cls := "accordion-body",
          div(
            cls := "mb-3",
            // children <-- possibleFilter.signal.map(filter =>
            children <-- possibleFilter.events.toSignal(CompanyFilter.empty).map(filter =>
              optionsFun(filter).map(v => renderCheckbox(groupName, v))
            )
          )
        )
      )
    )
  private def renderCheckbox(groupName: String, value: String) =
    div(
      cls := "form-check",
      label(
        cls   := "form-check-label",
        forId := s"filter-$groupName-$value",
        value
      ),
      input(
        cls    := "form-check-input",
        `type` := "checkbox",
        idAttr := s"filter-$groupName-$value",
        onChange.mapToChecked.map(checked =>
          CheckValueEvent(groupName, value, checked)
        ) --> checkEvents
        // selected <-- state.changes.map(_.countries.contains(value))
      )
    )
}
