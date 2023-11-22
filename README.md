# tech-articles

Code build while trying to write notes/articles on topics I am trying to understand, or I have something to add to.

## Maven wrapper plugin

The scope is to showcase how you can make a wrapper for an existing plugin in the case you want to add or adapt some
functionalities from that plugin.

The wrapper plugin consists of two modules:

* html-to-pdf-wrapper-plugin - a module consisting of a plugin that wraps an existing plugin
  au.net.causal.maven.plugins:html2pdf-maven-plugin:2.0
* html-to-pdf-wrapper-plugin-client - a module which uses the generated plugin in the previous module to convert a html
  file to a pdf file.

To mention that the wrapped plugin just to showcase how to wrap a maven plugin, there probably exist other plugins which
can convert a HTML to a PDF and are also actively maintained, the one used in this example does not seem to be actively
maintained for quite some years.

##