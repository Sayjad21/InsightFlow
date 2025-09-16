import React, { useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import {
  ChevronRight,
  BarChart3,
  Users,
  Target,
  Shield,
  ArrowRight,
  Star,
  TrendingUp,
  Brain,
  Search,
  Database,
  Github,
  Linkedin,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";

const Landing: React.FC = () => {
  const featuresRef = useRef<HTMLDivElement>(null);
  const { isAuthenticated } = useAuth();

  // Add smooth scrolling animation on load
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("animate-fade-in-up");
          }
        });
      },
      { threshold: 0.1 }
    );

    const elements = document.querySelectorAll(".fade-in-on-scroll");
    elements.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      <div className="animate-fade-in">
        {/* Navigation Header */}
        <nav className="sticky top-0 z-50 bg-white/70 backdrop-blur-xl shadow-lg border-b border-white/20 supports-[backdrop-filter]:bg-white/60">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center py-4">
              <div className="flex items-center space-x-3 group">
                <div className="w-8 h-8 bg-gradient-to-br from-purple-600 via-indigo-600 to-blue-600 rounded-lg flex items-center justify-center shadow-lg group-hover:shadow-purple-500/25 transition-all duration-300 group-hover:scale-105">
                  <Brain className="h-5 w-5 text-white" />
                </div>
                <span className="text-xl font-bold bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 bg-clip-text text-transparent">
                  InsightFlow
                </span>
              </div>
              <div className="flex items-center space-x-6">
                {isAuthenticated ? (
                  <>
                    <Link
                      to="/dashboard"
                      className="relative text-gray-700 hover:text-indigo-600 transition-all duration-300 font-medium px-4 py-2 rounded-full hover:bg-indigo-50/50 group"
                    >
                      <span className="relative z-10">Dashboard</span>
                      <div className="absolute inset-0 bg-gradient-to-r from-purple-100/0 to-blue-100/0 group-hover:from-purple-100/50 group-hover:to-blue-100/50 rounded-full transition-all duration-300"></div>
                    </Link>
                    <Link
                      to="/analysis"
                      className="relative bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 text-white px-6 py-2.5 rounded-full hover:from-purple-700 hover:via-indigo-700 hover:to-blue-700 transition-all duration-300 font-medium shadow-lg hover:shadow-xl hover:shadow-purple-500/25 transform hover:-translate-y-0.5 group overflow-hidden"
                    >
                      <span className="relative z-10">New Analysis</span>
                      <div className="absolute inset-0 bg-gradient-to-r from-white/0 to-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </Link>
                  </>
                ) : (
                  <>
                    <Link
                      to="/signin"
                      className="relative text-gray-700 hover:text-indigo-600 transition-all duration-300 font-medium px-4 py-2 rounded-full hover:bg-indigo-50/50 group"
                    >
                      <span className="relative z-10">Sign In</span>
                      <div className="absolute inset-0 bg-gradient-to-r from-purple-100/0 to-blue-100/0 group-hover:from-purple-100/50 group-hover:to-blue-100/50 rounded-full transition-all duration-300"></div>
                    </Link>
                    <Link
                      to="/signup"
                      className="relative bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 text-white px-6 py-2.5 rounded-full hover:from-purple-700 hover:via-indigo-700 hover:to-blue-700 transition-all duration-300 font-medium shadow-lg hover:shadow-xl hover:shadow-purple-500/25 transform hover:-translate-y-0.5 group overflow-hidden"
                    >
                      <span className="relative z-10">Get Started</span>
                      <div className="absolute inset-0 bg-gradient-to-r from-white/0 to-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </Link>
                  </>
                )}
              </div>
            </div>
          </div>
        </nav>

        {/* Hero Section */}
        <section className="relative overflow-hidden bg-gradient-to-br from-slate-50 via-purple-50/30 to-indigo-100/40">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 lg:py-32">
            <div className="text-center">
              {/* Animated background elements */}
              <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-4 -left-4 w-96 h-96 bg-gradient-to-br from-purple-400/30 via-indigo-400/20 to-blue-400/30 rounded-full mix-blend-multiply filter blur-3xl opacity-60 animate-blob"></div>
                <div className="absolute -top-4 -right-4 w-96 h-96 bg-gradient-to-br from-blue-400/30 via-cyan-400/20 to-indigo-400/30 rounded-full mix-blend-multiply filter blur-3xl opacity-60 animate-blob animation-delay-2000"></div>
                <div className="absolute -bottom-8 left-20 w-96 h-96 bg-gradient-to-br from-indigo-400/30 via-purple-400/20 to-pink-400/30 rounded-full mix-blend-multiply filter blur-3xl opacity-60 animate-blob animation-delay-4000"></div>
                <div className="absolute top-1/2 right-1/4 w-64 h-64 bg-gradient-to-br from-cyan-300/20 to-blue-300/20 rounded-full mix-blend-multiply filter blur-2xl opacity-40 animate-pulse"></div>
                <div className="absolute bottom-1/3 left-1/3 w-48 h-48 bg-gradient-to-br from-pink-300/20 to-purple-300/20 rounded-full mix-blend-multiply filter blur-2xl opacity-40 animate-bounce"></div>
              </div>

              <div className="relative z-10">
                <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold text-gray-900 mb-8 leading-tight tracking-tight">
                  {isAuthenticated ? (
                    <>
                      <span className="block animate-fade-in-up">
                        Welcome Back to
                      </span>
                      <span className="block bg-gradient-to-r from-purple-600 via-indigo-600 to-cyan-600 bg-clip-text text-transparent animate-gradient-x">
                        Your Analytics Hub
                      </span>
                    </>
                  ) : (
                    <>
                      <span className="block animate-fade-in-up">
                        Transform Data Into
                      </span>
                      <span className="block bg-gradient-to-r from-purple-600 via-indigo-600 to-cyan-600 bg-clip-text text-transparent animate-gradient-x">
                        Actionable Insights
                      </span>
                    </>
                  )}
                </h1>
                <p className="text-xl sm:text-2xl text-gray-700/80 mb-12 max-w-4xl mx-auto leading-relaxed font-light tracking-wide">
                  {isAuthenticated
                    ? "Continue your journey with AI-driven analysis. Create new insights, track your progress, and make informed decisions with your comprehensive analytics dashboard."
                    : "Harness the power of AI-driven analysis to unlock deep insights from your data. Make informed decisions with comprehensive reports and intelligent comparisons."}
                </p>
                <div className="flex flex-col sm:flex-row gap-6 justify-center items-center">
                  {isAuthenticated ? (
                    <>
                      <Link
                        to="/dashboard"
                        className="group relative bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 text-white px-10 py-4 rounded-2xl text-lg font-semibold hover:from-purple-700 hover:via-indigo-700 hover:to-blue-700 transition-all duration-300 shadow-2xl hover:shadow-purple-500/25 transform hover:-translate-y-1 hover:scale-105 flex items-center overflow-hidden"
                      >
                        <span className="relative z-10">Go to Dashboard</span>
                        <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform relative z-10" />
                        <div className="absolute inset-0 bg-gradient-to-r from-white/0 to-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                      </Link>
                      <Link
                        to="/analysis"
                        className="group relative text-gray-700 px-10 py-4 rounded-2xl text-lg font-semibold border-2 border-gray-200/60 backdrop-blur-sm hover:border-purple-300 hover:bg-white/70 hover:shadow-xl hover:shadow-purple-500/10 transition-all duration-300 flex items-center"
                      >
                        <span className="relative z-10">
                          Create New Analysis
                        </span>
                        <ChevronRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
                      </Link>
                    </>
                  ) : (
                    <>
                      <Link
                        to="/signup"
                        className="group relative bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 text-white px-10 py-4 rounded-2xl text-lg font-semibold hover:from-purple-700 hover:via-indigo-700 hover:to-blue-700 transition-all duration-300 shadow-2xl hover:shadow-purple-500/25 transform hover:-translate-y-1 hover:scale-105 flex items-center overflow-hidden"
                      >
                        <span className="relative z-10">
                          Start Free Analysis
                        </span>
                        <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform relative z-10" />
                        <div className="absolute inset-0 bg-gradient-to-r from-white/0 to-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                      </Link>
                      <button
                        onClick={() =>
                          featuresRef.current?.scrollIntoView({
                            behavior: "smooth",
                          })
                        }
                        className="group relative text-gray-700 px-10 py-4 rounded-2xl text-lg font-semibold border-2 border-gray-200/60 backdrop-blur-sm hover:border-purple-300 hover:bg-white/70 hover:shadow-xl hover:shadow-purple-500/10 transition-all duration-300 flex items-center"
                      >
                        <span className="relative z-10">Learn More</span>
                        <ChevronRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Floating Cards Preview */}
          <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-20 pb-20">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 opacity-90">
              <div className="group bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-xl hover:shadow-2xl hover:shadow-purple-500/20 transition-all duration-500 transform hover:-translate-y-4 hover:rotate-1 border border-white/40">
                <div className="w-14 h-14 bg-gradient-to-br from-purple-500 via-purple-600 to-indigo-600 rounded-2xl flex items-center justify-center mb-6 shadow-lg group-hover:shadow-purple-500/30 group-hover:scale-110 transition-all duration-300">
                  <Search className="h-7 w-7 text-white" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3 group-hover:text-purple-700 transition-colors">
                  Smart Analysis
                </h3>
                <p className="text-gray-600 group-hover:text-gray-700 leading-relaxed">
                  AI-powered insights that reveal hidden patterns and trends in
                  your data with unprecedented accuracy.
                </p>
                <div className="absolute inset-0 bg-gradient-to-br from-purple-500/5 to-indigo-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-3xl"></div>
              </div>
              <div className="group bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-xl hover:shadow-2xl hover:shadow-blue-500/20 transition-all duration-500 transform hover:-translate-y-4 hover:-rotate-1 md:mt-8 border border-white/40">
                <div className="w-14 h-14 bg-gradient-to-br from-blue-500 via-blue-600 to-cyan-600 rounded-2xl flex items-center justify-center mb-6 shadow-lg group-hover:shadow-blue-500/30 group-hover:scale-110 transition-all duration-300">
                  <BarChart3 className="h-7 w-7 text-white" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3 group-hover:text-blue-700 transition-colors">
                  Visual Reports
                </h3>
                <p className="text-gray-600 group-hover:text-gray-700 leading-relaxed">
                  Beautiful, interactive dashboards that make complex data easy
                  to understand and actionable.
                </p>
                <div className="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-cyan-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-3xl"></div>
              </div>
              <div className="group bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-xl hover:shadow-2xl hover:shadow-indigo-500/20 transition-all duration-500 transform hover:-translate-y-4 hover:rotate-1 border border-white/40">
                <div className="w-14 h-14 bg-gradient-to-br from-indigo-500 via-indigo-600 to-purple-600 rounded-2xl flex items-center justify-center mb-6 shadow-lg group-hover:shadow-indigo-500/30 group-hover:scale-110 transition-all duration-300">
                  <TrendingUp className="h-7 w-7 text-white" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3 group-hover:text-indigo-700 transition-colors">
                  Predictive Insights
                </h3>
                <p className="text-gray-600 group-hover:text-gray-700 leading-relaxed">
                  Forecast trends and identify opportunities before your
                  competition with machine learning.
                </p>
                <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/5 to-purple-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-3xl"></div>
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section
          ref={featuresRef}
          className="py-24 bg-gradient-to-br from-white via-gray-50/50 to-purple-50/30"
        >
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-20 fade-in-on-scroll">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-purple-100 to-blue-100 rounded-2xl mb-6">
                <Brain className="h-8 w-8 text-purple-600" />
              </div>
              <h2 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-gray-900 mb-8 tracking-tight">
                Everything You Need to{" "}
                <span className="bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 bg-clip-text text-transparent">
                  Make Better Decisions
                </span>
              </h2>
              <p className="text-xl text-gray-600 max-w-4xl mx-auto leading-relaxed">
                Our comprehensive suite of analysis tools empowers you to
                transform raw data into strategic insights that drive
                exponential growth.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
              {[
                {
                  icon: Database,
                  title: "Multi-Source Integration",
                  description:
                    "Connect and analyze data from multiple sources seamlessly. Support for various file formats and data types with real-time synchronization.",
                  color: "from-purple-500 to-purple-600",
                  hoverColor: "group-hover:shadow-purple-500/25",
                  bgGradient: "from-purple-500/5 to-purple-600/5",
                },
                {
                  icon: Brain,
                  title: "AI-Powered Analysis",
                  description:
                    "Leverage advanced machine learning algorithms to uncover insights that traditional analysis might miss with cutting-edge neural networks.",
                  color: "from-blue-500 to-blue-600",
                  hoverColor: "group-hover:shadow-blue-500/25",
                  bgGradient: "from-blue-500/5 to-blue-600/5",
                },
                {
                  icon: BarChart3,
                  title: "Interactive Dashboards",
                  description:
                    "Create stunning, interactive visualizations that tell your data's story clearly and compellingly with real-time updates.",
                  color: "from-indigo-500 to-indigo-600",
                  hoverColor: "group-hover:shadow-indigo-500/25",
                  bgGradient: "from-indigo-500/5 to-indigo-600/5",
                },
                {
                  icon: Users,
                  title: "Collaborative Workspace",
                  description:
                    "Share insights with your team and collaborate on analysis projects in real-time with advanced permission controls.",
                  color: "from-purple-500 to-blue-500",
                  hoverColor: "group-hover:shadow-purple-500/25",
                  bgGradient: "from-purple-500/5 to-blue-500/5",
                },
                {
                  icon: Target,
                  title: "Goal Tracking",
                  description:
                    "Set and monitor key performance indicators with automated alerts and progress tracking that keeps you on target.",
                  color: "from-blue-500 to-indigo-500",
                  hoverColor: "group-hover:shadow-blue-500/25",
                  bgGradient: "from-blue-500/5 to-indigo-500/5",
                },
                {
                  icon: Shield,
                  title: "Enterprise Security",
                  description:
                    "Bank-level security with end-to-end encryption and compliance with industry standards including SOC 2 and GDPR.",
                  color: "from-indigo-500 to-purple-500",
                  hoverColor: "group-hover:shadow-indigo-500/25",
                  bgGradient: "from-indigo-500/5 to-purple-500/5",
                },
              ].map((feature, index) => (
                <div
                  key={index}
                  className={`fade-in-on-scroll relative bg-white/80 backdrop-blur-sm rounded-3xl p-8 hover:bg-white hover:shadow-2xl ${feature.hoverColor} transition-all duration-500 group cursor-pointer border border-gray-100/50 hover:border-white overflow-hidden`}
                >
                  <div
                    className={`absolute inset-0 bg-gradient-to-br ${feature.bgGradient} opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-3xl`}
                  ></div>
                  <div className="relative z-10">
                    <div
                      className={`w-16 h-16 bg-gradient-to-br ${feature.color} rounded-3xl flex items-center justify-center mb-6 group-hover:scale-110 group-hover:rotate-3 transition-all duration-300 shadow-lg`}
                    >
                      <feature.icon className="h-8 w-8 text-white" />
                    </div>
                    <h3 className="text-2xl font-bold text-gray-900 mb-4 group-hover:text-purple-700 transition-colors">
                      {feature.title}
                    </h3>
                    <p className="text-gray-600 group-hover:text-gray-700 leading-relaxed text-lg">
                      {feature.description}
                    </p>
                  </div>
                  <div className="absolute -top-10 -right-10 w-20 h-20 bg-gradient-to-br from-white/10 to-white/5 rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Stats Section */}
        <section className="py-24 bg-gradient-to-br from-purple-600 via-indigo-700 to-blue-800 relative overflow-hidden">
          {/* Background Pattern */}
          <div className="absolute inset-0 opacity-10">
            <div
              className="w-full h-full"
              style={{
                backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='m36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`,
              }}
            ></div>
          </div>

          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div className="text-center mb-16">
              <h2 className="text-3xl sm:text-4xl font-bold text-white mb-4">
                Trusted by{" "}
                <span className="bg-gradient-to-r from-cyan-300 to-blue-300 bg-clip-text text-transparent">
                  Industry Leaders
                </span>
              </h2>
              <p className="text-purple-100 text-lg max-w-2xl mx-auto">
                Join thousands of organizations making data-driven decisions
                with InsightFlow
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-8 text-center">
              {[
                { number: "10K+", label: "Active Users", icon: Users },
                {
                  number: "1M+",
                  label: "Data Points Analyzed",
                  icon: Database,
                },
                { number: "99.9%", label: "Uptime Guarantee", icon: Shield },
                { number: "24/7", label: "Expert Support", icon: Target },
              ].map((stat, index) => (
                <div key={index} className="fade-in-on-scroll group">
                  <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-8 hover:bg-white/20 hover:scale-105 transition-all duration-300 border border-white/20">
                    <div className="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-white/20 to-white/10 rounded-xl flex items-center justify-center group-hover:from-white/30 group-hover:to-white/20 transition-all duration-300">
                      <stat.icon className="h-6 w-6 text-white" />
                    </div>
                    <div className="text-4xl sm:text-5xl font-bold text-white mb-3 group-hover:scale-110 transition-transform duration-300">
                      {stat.number}
                    </div>
                    <div className="text-purple-100 text-lg font-medium">
                      {stat.label}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Testimonials */}
        <section className="py-24 bg-gradient-to-br from-gray-50 via-white to-purple-50/30">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-20 fade-in-on-scroll">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-purple-100 to-blue-100 rounded-2xl mb-6">
                <Star className="h-8 w-8 text-purple-600" />
              </div>
              <h2 className="text-4xl sm:text-5xl font-bold text-gray-900 mb-8 tracking-tight">
                What Our{" "}
                <span className="bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent">
                  Customers Say
                </span>
              </h2>
              <p className="text-xl text-gray-600 max-w-3xl mx-auto">
                See how InsightFlow is transforming businesses across industries
                with powerful data insights.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {[
                {
                  quote:
                    "InsightFlow transformed how we analyze our business data. The AI insights are incredibly accurate and actionable, helping us increase revenue by 40%.",
                  author: "Sarah Chen",
                  role: "VP of Analytics, TechCorp",
                  rating: 5,
                  gradient: "from-purple-500/10 to-indigo-500/10",
                  border: "border-purple-200/50",
                },
                {
                  quote:
                    "The collaborative features make it easy for our entire team to work with data. Game-changing platform that increased our efficiency by 60%!",
                  author: "Marcus Johnson",
                  role: "Data Director, Innovate Inc",
                  rating: 5,
                  gradient: "from-blue-500/10 to-cyan-500/10",
                  border: "border-blue-200/50",
                },
                {
                  quote:
                    "Implementation was seamless and the support team is exceptional. Highly recommend for any data-driven organization looking to scale.",
                  author: "Emily Rodriguez",
                  role: "CTO, Future Solutions",
                  rating: 5,
                  gradient: "from-indigo-500/10 to-purple-500/10",
                  border: "border-indigo-200/50",
                },
              ].map((testimonial, index) => (
                <div
                  key={index}
                  className={`fade-in-on-scroll group relative bg-white/80 backdrop-blur-sm rounded-3xl p-8 shadow-lg hover:shadow-2xl hover:shadow-purple-500/10 transition-all duration-500 border ${testimonial.border} hover:border-white overflow-hidden hover:-translate-y-2`}
                >
                  <div
                    className={`absolute inset-0 bg-gradient-to-br ${testimonial.gradient} opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-3xl`}
                  ></div>
                  <div className="relative z-10">
                    <div className="flex mb-6">
                      {[...Array(testimonial.rating)].map((_, i) => (
                        <Star
                          key={i}
                          className="h-6 w-6 text-yellow-400 fill-current group-hover:scale-110 transition-transform duration-200"
                          style={{ transitionDelay: `${i * 50}ms` }}
                        />
                      ))}
                    </div>
                    <p className="text-gray-700 mb-8 italic text-lg leading-relaxed font-medium">
                      "{testimonial.quote}"
                    </p>
                    <div className="border-t border-gray-100 pt-6">
                      <div className="font-bold text-gray-900 text-lg mb-1">
                        {testimonial.author}
                      </div>
                      <div className="text-gray-500 font-medium">
                        {testimonial.role}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="py-24 bg-gradient-to-br from-slate-900 via-purple-900 to-indigo-900 relative overflow-hidden">
          {/* Enhanced background effects */}
          <div className="absolute inset-0">
            <div className="absolute inset-0 bg-gradient-to-r from-purple-900/50 to-indigo-900/50"></div>
            <div className="absolute top-0 left-0 w-full h-full bg-[radial-gradient(circle_at_50%_50%,_rgba(139,_69,_199,_0.3),_transparent_50%)]"></div>
            <div className="absolute -top-40 -left-40 w-80 h-80 bg-purple-500/20 rounded-full blur-3xl"></div>
            <div className="absolute -bottom-40 -right-40 w-80 h-80 bg-blue-500/20 rounded-full blur-3xl"></div>
          </div>

          <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
            <div className="fade-in-on-scroll">
              {isAuthenticated ? (
                <>
                  <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-purple-500/20 to-blue-500/20 backdrop-blur-sm rounded-3xl mb-8 border border-purple-400/20">
                    <Brain className="h-10 w-10 text-purple-300" />
                  </div>
                  <h2 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-white mb-10 tracking-tight leading-tight">
                    Ready to Create Your Next{" "}
                    <span className="bg-gradient-to-r from-purple-400 via-pink-400 to-blue-400 bg-clip-text text-transparent">
                      Analysis?
                    </span>
                  </h2>
                  <p className="text-xl text-gray-300 mb-16 max-w-3xl mx-auto leading-relaxed">
                    Continue your journey with InsightFlow. Create new analyses,
                    view your dashboard, or explore your past insights with
                    advanced AI capabilities.
                  </p>
                  <div className="flex flex-col sm:flex-row gap-6 justify-center items-center">
                    <Link
                      to="/analysis"
                      className="group relative bg-gradient-to-r from-purple-600 via-pink-600 to-blue-600 text-white px-10 py-5 rounded-2xl text-xl font-semibold hover:from-purple-500 hover:via-pink-500 hover:to-blue-500 transition-all duration-300 shadow-2xl hover:shadow-purple-500/25 transform hover:-translate-y-1 hover:scale-105 flex items-center overflow-hidden"
                    >
                      <span className="relative z-10">Create New Analysis</span>
                      <ArrowRight className="ml-3 h-6 w-6 group-hover:translate-x-1 transition-transform relative z-10" />
                      <div className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/20 to-white/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </Link>
                    <Link
                      to="/dashboard"
                      className="relative text-gray-300 px-10 py-5 rounded-2xl text-xl font-semibold border-2 border-gray-500/50 backdrop-blur-sm hover:border-purple-400/50 hover:bg-purple-500/10 hover:text-white transition-all duration-300 flex items-center hover:shadow-lg hover:shadow-purple-500/10"
                    >
                      <span className="relative z-10">View Dashboard</span>
                    </Link>
                  </div>
                  <p className="text-gray-400 mt-10 text-lg">
                    <span className="inline-flex items-center space-x-2">
                      <Star className="h-5 w-5 text-yellow-400" />
                      <span>Welcome back!</span>
                    </span>
                    {" • "}
                    <span className="inline-flex items-center space-x-2">
                      <TrendingUp className="h-5 w-5 text-green-400" />
                      <span>Your insights await</span>
                    </span>
                    {" • "}
                    <span className="inline-flex items-center space-x-2">
                      <Brain className="h-5 w-5 text-purple-400" />
                      <span>Let's analyze together</span>
                    </span>
                  </p>
                </>
              ) : (
                <>
                  <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-purple-500/20 to-blue-500/20 backdrop-blur-sm rounded-3xl mb-8 border border-purple-400/20">
                    <Brain className="h-10 w-10 text-purple-300" />
                  </div>
                  <h2 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-white mb-10 tracking-tight leading-tight">
                    Ready to Unlock Your Data's{" "}
                    <span className="bg-gradient-to-r from-purple-400 via-pink-400 to-blue-400 bg-clip-text text-transparent">
                      Full Potential?
                    </span>
                  </h2>
                  <p className="text-xl text-gray-300 mb-16 max-w-3xl mx-auto leading-relaxed">
                    Join thousands of organizations already using InsightFlow to
                    make smarter, data-driven decisions every day with
                    cutting-edge AI technology.
                  </p>
                  <div className="flex flex-col sm:flex-row gap-6 justify-center items-center">
                    <Link
                      to="/signup"
                      className="group relative bg-gradient-to-r from-purple-600 via-pink-600 to-blue-600 text-white px-10 py-5 rounded-2xl text-xl font-semibold hover:from-purple-500 hover:via-pink-500 hover:to-blue-500 transition-all duration-300 shadow-2xl hover:shadow-purple-500/25 transform hover:-translate-y-1 hover:scale-105 flex items-center overflow-hidden"
                    >
                      <span className="relative z-10">Start Free Trial</span>
                      <ArrowRight className="ml-3 h-6 w-6 group-hover:translate-x-1 transition-transform relative z-10" />
                      <div className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/20 to-white/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </Link>
                    <Link
                      to="/signin"
                      className="relative text-gray-300 px-10 py-5 rounded-2xl text-xl font-semibold border-2 border-gray-500/50 backdrop-blur-sm hover:border-purple-400/50 hover:bg-purple-500/10 hover:text-white transition-all duration-300 flex items-center hover:shadow-lg hover:shadow-purple-500/10"
                    >
                      <span className="relative z-10">Sign In</span>
                    </Link>
                  </div>
                  <p className="text-gray-400 mt-10 text-lg">
                    <span className="inline-flex items-center space-x-2">
                      <Shield className="h-5 w-5 text-green-400" />
                      <span>No credit card required</span>
                    </span>
                    {" • "}
                    <span className="inline-flex items-center space-x-2">
                      <Star className="h-5 w-5 text-yellow-400" />
                      <span>14-day free trial</span>
                    </span>
                    {" • "}
                    <span className="inline-flex items-center space-x-2">
                      <Target className="h-5 w-5 text-purple-400" />
                      <span>Cancel anytime</span>
                    </span>
                  </p>
                </>
              )}
            </div>
          </div>
        </section>

        {/* Footer */}
        <footer className="bg-gradient-to-br from-gray-900 via-slate-900 to-gray-900 text-white py-16 relative overflow-hidden">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_80%,_rgba(120,_119,_198,_0.1),_transparent_50%)]"></div>
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_80%_20%,_rgba(59,_130,_246,_0.1),_transparent_50%)]"></div>

          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-10 mb-12">
              <div className="col-span-1 md:col-span-2">
                <div className="flex items-center space-x-3 mb-6">
                  <div className="w-10 h-10 bg-gradient-to-br from-purple-600 via-indigo-600 to-blue-600 rounded-2xl flex items-center justify-center shadow-lg">
                    <Brain className="h-6 w-6 text-white" />
                  </div>
                  <span className="text-2xl font-bold bg-gradient-to-r from-purple-400 to-blue-400 bg-clip-text text-transparent">
                    InsightFlow
                  </span>
                </div>
                <p className="text-gray-400 max-w-md leading-relaxed text-lg mb-6">
                  Empowering organizations with AI-driven insights to make
                  better decisions and drive exponential growth through
                  intelligent data analysis and predictive analytics.
                </p>
                <div className="flex space-x-4">
                  {/* GitHub */}
                  <a
                    href="https://github.com/Sayjad21/InsightFlow"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="w-10 h-10 bg-gray-800 hover:bg-purple-600 rounded-full flex items-center justify-center transition-colors duration-300 cursor-pointer"
                  >
                    <Github className="w-5 h-5 text-gray-400 group-hover:text-white transition-colors duration-300" />
                  </a>

                  {/* LinkedIn */}
                  <a
                    href="https://www.linkedin.com/"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="w-10 h-10 bg-gray-800 hover:bg-blue-600 rounded-full flex items-center justify-center transition-colors duration-300 cursor-pointer"
                  >
                    <Linkedin className="w-5 h-5 text-gray-400 group-hover:text-white transition-colors duration-300" />
                  </a>
                </div>
              </div>
              <div>
                <h4 className="font-bold mb-6 text-white text-lg">Product</h4>
                <ul className="space-y-3 text-gray-400">
                  <li>
                    <a
                      href="#"
                      className="hover:text-white transition-colors duration-300 hover:translate-x-1 transform inline-block"
                    >
                      Features
                    </a>
                  </li>
                  <li>
                    <a
                      href="#"
                      className="hover:text-white transition-colors duration-300 hover:translate-x-1 transform inline-block"
                    >
                      Pricing
                    </a>
                  </li>
                  <li>
                    <a
                      href="#"
                      className="hover:text-white transition-colors duration-300 hover:translate-x-1 transform inline-block"
                    >
                      API Documentation
                    </a>
                  </li>
                  <li>
                    <a
                      href="#"
                      className="hover:text-white transition-colors duration-300 hover:translate-x-1 transform inline-block"
                    >
                      Integrations
                    </a>
                  </li>
                </ul>
              </div>
              <div>
                <h4 className="font-bold mb-6 text-white text-lg">Support</h4>
                <ul className="space-y-3 text-gray-400">
                  <li>
                    <a
                      href="#"
                      className="hover:text-white transition-colors duration-300 hover:translate-x-1 transform inline-block"
                    >
                      Help Center
                    </a>
                  </li>
                  <li>
                    <a
                      href="#"
                      className="hover:text-white transition-colors duration-300 hover:translate-x-1 transform inline-block"
                    >
                      Contact Us
                    </a>
                  </li>
                  <li>
                    <a
                      href="#"
                      className="hover:text-white transition-colors duration-300 hover:translate-x-1 transform inline-block"
                    >
                      Privacy Policy
                    </a>
                  </li>
                  <li>
                    <a
                      href="#"
                      className="hover:text-white transition-colors duration-300 hover:translate-x-1 transform inline-block"
                    >
                      Terms of Service
                    </a>
                  </li>
                </ul>
              </div>
            </div>
            <div className="border-t border-gray-800 pt-8 text-center">
              <p className="text-gray-400 text-lg">
                &copy; 2025 InsightFlow. All rights reserved. • Powered by{" "}
                <span className="text-purple-400 font-semibold">Ollama</span> &{" "}
                <span className="text-blue-400 font-semibold">Tavily</span> •
                Built with ❤️ for data-driven decisions
              </p>
            </div>
          </div>
        </footer>

        {/* Custom Styles */}
        <style>{`
        @keyframes blob {
          0% {
            transform: translate(0px, 0px) scale(1);
          }
          33% {
            transform: translate(30px, -50px) scale(1.1);
          }
          66% {
            transform: translate(-20px, 20px) scale(0.9);
          }
          100% {
            transform: translate(0px, 0px) scale(1);
          }
        }

        @keyframes gradient-x {
          0%, 100% {
            background-size: 200% 200%;
            background-position: left center;
          }
          50% {
            background-size: 200% 200%;
            background-position: right center;
          }
        }

        @keyframes fadeInUp {
          from {
            opacity: 0;
            transform: translateY(30px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes fadeInUpCustom {
          0% {
            opacity: 0;
            transform: translateY(20px);
          }
          100% {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes float {
          0%, 100% {
            transform: translateY(0px);
          }
          50% {
            transform: translateY(-10px);
          }
        }

        @keyframes shimmer {
          0% {
            background-position: -1000px 0;
          }
          100% {
            background-position: 1000px 0;
          }
        }

        @keyframes pulse-soft {
          0%, 100% {
            transform: scale(1);
            opacity: 1;
          }
          50% {
            transform: scale(1.05);
            opacity: 0.8;
          }
        }

        .animate-blob {
          animation: blob 7s infinite;
        }

        .animate-gradient-x {
          animation: gradient-x 3s ease infinite;
          background-size: 200% 200%;
        }

        .animation-delay-2000 {
          animation-delay: 2s;
        }

        .animation-delay-4000 {
          animation-delay: 4s;
        }

        .animate-fade-in-up {
          animation: fadeInUp 0.8s ease-out forwards;
        }

        .animate-fade-in {
          animation: fadeInUpCustom 0.9s ease-out both;
        }

        .animate-float {
          animation: float 3s ease-in-out infinite;
        }

        .animate-pulse-soft {
          animation: pulse-soft 2s ease-in-out infinite;
        }

        .fade-in-on-scroll {
          opacity: 0;
          transform: translateY(30px);
          transition: opacity 0.8s ease-out, transform 0.8s ease-out;
        }

        .animate-shimmer {
          background: linear-gradient(
            90deg,
            transparent,
            rgba(255, 255, 255, 0.4),
            transparent
          );
          background-size: 200px 100%;
          background-repeat: no-repeat;
          animation: shimmer 2s infinite;
        }

        /* Glass morphism effects */
        .glass {
          background: rgba(255, 255, 255, 0.1);
          backdrop-filter: blur(10px);
          border: 1px solid rgba(255, 255, 255, 0.2);
        }

        .glass-dark {
          background: rgba(0, 0, 0, 0.1);
          backdrop-filter: blur(10px);
          border: 1px solid rgba(255, 255, 255, 0.1);
        }

        /* Enhanced hover effects */
        .hover-lift {
          transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        }

        .hover-lift:hover {
          transform: translateY(-8px);
          box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
        }

        /* Gradient text effects */
        .gradient-text-animate {
          background: linear-gradient(45deg, #8B5CF6, #3B82F6, #06B6D4, #8B5CF6);
          background-size: 400% 400%;
          animation: gradient-x 4s ease infinite;
          -webkit-background-clip: text;
          background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        /* Smooth scrolling */
        html {
          scroll-behavior: smooth;
        }

        /* Custom scrollbar */
        ::-webkit-scrollbar {
          width: 8px;
        }

        ::-webkit-scrollbar-track {
          background: #f1f5f9;
        }

        ::-webkit-scrollbar-thumb {
          background: linear-gradient(to bottom, #8B5CF6, #3B82F6);
          border-radius: 10px;
        }

        ::-webkit-scrollbar-thumb:hover {
          background: linear-gradient(to bottom, #7C3AED, #2563EB);
        }
      `}</style>
      </div>
    </div>
  );
};

export default Landing;
