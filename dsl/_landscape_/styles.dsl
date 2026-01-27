element "Element" {
  color #0773af
  stroke #0773af
  strokeWidth 7
  background #fefffe
  shape roundedbox
}
element "Boundary" {
  strokeWidth 5
}
relationship "Relationship" {
  thickness 4
}
element "Person" {
  shape person
}
element "Webpage" {
  shape WebBrowser
}
element "Database" {
  shape cylinder
}
element "Messaging" {
  shape pipe
}
element "future" {
  background #00b38a
  stroke #00b38a
}
element "legacy" {
  background #bdbdbd
  stroke #bdbdbd
}
element "external" {
  background #a0414b
  stroke #a0414b
}
relationship "legacy" {
  color #bdbdbd
  style dotted
}
relationship "future" {
  color #00b38a
  style dotted
}
# !include external_logos.dsl
