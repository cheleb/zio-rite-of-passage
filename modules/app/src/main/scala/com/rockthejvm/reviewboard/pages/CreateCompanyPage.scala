package com.rockthejvm.reviewboard.pages

import zio.*
import zio.prelude.ZValidation.Failure
import zio.prelude.ZValidation.Success
import zio.prelude.*

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.http.requests.*
import org.scalajs.dom
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.HTMLImageElement
import org.scalajs.dom.html
import org.scalajs.dom.File
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints

case class CreateCompanyState(
    name: String = "",
    url: String = "",
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: List[String] = Nil,
    upstreamStatus: Option[Either[String, String]] = None,
    val showStatus: Boolean = false
) extends FormState {

  private def nameValidation = Validation.fromEither(
    if name.nonEmpty then Right(name)
    else Left("Name must be provided")
  )

  private def urlValidation = Validation.fromEither(
    if url.matches(Constants.urlRegex) then Right(url)
    else Left("A valid URL must be provided")
  )

  private lazy val validate =
    Validation.validate(
      nameValidation,
      urlValidation,
      Validation.fromEither(upstreamStatus.flatMap(_.left.toOption).toLeft(()))
    )

  def validationErrors: List[String] = validate match
    case Failure(_, errors) =>
      errors.toList
    case Success(_, _) => Nil

  override def hasErrors: Boolean = validationErrors.nonEmpty

  override def maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)

}

object CreateCompanyPage extends SecuredFormPage[CreateCompanyState]("Create Company") {

  override def basicState: CreateCompanyState = CreateCompanyState()

  private val fileUploader = Observer[List[File]] { files =>
    files.headOption match
      case None       => stateVar.update(_.copy(image = None))
      case Some(file) =>
        val reader = new dom.FileReader()
        reader.onload = _ => {
          val fakeImg = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
          fakeImg.addEventListener(
            "load",
            _ => {
              val canvas          = dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
              val (width, height) = computeDimensions(fakeImg.width, fakeImg.height)
              canvas.width = width
              canvas.height = height
              val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
              ctx.drawImage(fakeImg, 0, 0, width, height)
              val dataUrl = canvas.toDataURL(file.`type`)
              stateVar.update(_.copy(image = Option(dataUrl)))
            }
          )
          fakeImg.src = reader.result.toString
        }
        reader.readAsDataURL(file)

  }

  override def renderChildren(user: UserToken): List[ReactiveHtmlElement[html.Element]] = List(
    renderInput("Company name", "Name", "text", true, "ACME Inc", (s, v) => s.copy(name = v)),
    renderInput("Company URL", "URL", "text", true, "https://acme.com", (s, v) => s.copy(url = v)),
    renderLogoUpload("logo", "logo", false),
    img(
      src <-- stateVar.signal.map(_.image.getOrElse("")),
      width  := "256",
      height := "256"
    ),
    renderInput("Location", "Location", "text", false, "San Francisco", (s, v) => s.copy(location = Option(v))),
    renderInput("Country", "Country", "text", false, "USA", (s, v) => s.copy(country = Option(v))),
    renderInput("Industry", "Industry", "text", false, "Tech", (s, v) => s.copy(industry = Option(v))),
    renderInput("Image URL", "Image", "text", false, "https://acme.com/logo.png", (s, v) => s.copy(image = Option(v))),
    renderInput(
      "Tags - comma separated",
      "Tags",
      "text",
      false,
      "tech, software, startup",
      (s, v) => s.copy(tags = v.split(",").map(_.trim()).toList.map(_.trim))
    ),
    button(
      `type` := "button",
      "Post company",
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    )
  )
  val submitter = Observer[CreateCompanyState] { state =>
    if state.hasErrors then
      println("Errors")
      stateVar.update(_.copy(showStatus = true))
    else
      CompanyEndpoints.create(CreateCompanyRequest(
        state.name,
        state.url,
        state.location,
        state.country,
        state.industry,
        state.image,
        Option(state.tags).filter(_.nonEmpty)
      ))
        .map { _ =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Right("Company posted successfully."))))
        }
        .tapError(e =>
          ZIO.succeed(stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Left(e.getMessage)))))
        )
        .runJs
  }
  def renderLogoUpload(name: String, uid: String, isRequired: Boolean) = div(
    cls := "row",
    div(
      cls := "col-md-12",
      div(
        cls := "form-input",
        label(
          forId := uid,
          cls   := "form-label",
          if isRequired then span("*") else span(),
          name
        ),
        div(
          cls := "image-upload",
          input(
            `type` := "file",
            cls    := "form-control",
            idAttr := uid,
            accept := "image/*",
            onChange.mapToFiles --> fileUploader
          ),
          img(
            cls := "image-upload-thumbnail",
            src <-- stateVar.signal.map(_.image.getOrElse(Constants.companyLogoPlaceHolder)),
            alt := "Preview"
          )
        )
      )
    )
  )
  private def computeDimensions(width: Int, height: Int): (Int, Int) =
    if width >= height then
      val aspectRatio = width * 1.0 / 128
      val newWidth    = width / aspectRatio
      val newHeight   = height / aspectRatio
      (newWidth.toInt, newHeight.toInt)
    else
      val (newHeight, newWidth) = computeDimensions(height, width)
      (newWidth, newHeight)
}
