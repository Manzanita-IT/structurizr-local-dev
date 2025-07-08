element "Person" {
  shape person
  background #082f6e
  color #ffffff
}
element "Software System" {
  background #082f6e
  color #ffffff
}
element "Container" {
  background #4a5ca2
  color #ffffff
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
}
element "legacy" {
  background #bdbdbd
}
element "external" {
  background #a0414b
}
relationship "legacy" {
  color #bdbdbd
  style dotted
}
relationship "future" {
  color #00b38a
  style dotted
}
