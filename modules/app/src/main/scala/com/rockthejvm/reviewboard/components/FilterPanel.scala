package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import org.scalajs.dom
import com.rockthejvm.reviewboard.domain.data.CompanyFilter
import com.rockthejvm.reviewboard.core.ZJS.*

/** A filter panel for the companies page.
  */
object FilterPanel {

  val possibleFilter = EventBus[CompanyFilter]()
//  val possibleFilter = Var[CompanyFilter](CompanyFilter.empty)

  def apply() = div(
    onMountCallback(_ =>
//      useBackend(_.company.allFiltersEndpoint(())).map(possibleFilter.set).runJs
      useBackend(_.company.allFiltersEndpoint(())).emitTo(possibleFilter)
    ),
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
          renderFilterOptions("Locations", _.locations),
          renderFilterOptions("Countries", _.countries),
          renderFilterOptions("Industries", _.industries),
          renderFilterOptions("Tags", _.tags),
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
        idAttr := s"filter-$groupName-$value"
      )
    )
}
